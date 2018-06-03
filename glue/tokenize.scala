import com.amazonaws.services.glue.GlueContext
import com.amazonaws.services.glue.util.GlueArgParser
import com.amazonaws.services.glue.util.Job
import org.apache.spark.SparkContext
import com.amazonaws.services.s3.AmazonS3ClientBuilder

import org.apache.spark.sql.functions._

import org.apache.spark.ml.feature.{RegexTokenizer, StopWordsRemover, CountVectorizer, CountVectorizerModel}

import scala.collection.JavaConverters._
import scala.xml.XML

object Tokenizer {
  def main(sysArgs: Array[String]) {
    val sc: SparkContext = new SparkContext()
    val glueContext: GlueContext = new GlueContext(sc)
    // @params: [JOB_NAME, sagemaker_bucket]
    println(sysArgs)
    val args = GlueArgParser.getResolvedOptions(sysArgs, Seq("JOB_NAME","sagemaker_bucket").toArray)
    val bucket = args.getOrElse("sagemaker_bucket","")
    Job.init(args("JOB_NAME"), glueContext, args.asJava)
    // User Code
    Job.commit()

    val spark = glueContext.getSparkSession
    import spark.implicits._

    val df = spark
        .read
        .option("header","true")
        .option("inferSchema","true")
        .option("escape","\"")
        .csv(s"s3://${bucket}/data/nyt-comments/CommentsMarch2018.csv")
        
    def remove_html(str: String): String = {
        XML.loadString(s"<p>${str}</p>").text
    }
    
    val clean_comment = udf[String, String](remove_html)

    val filtered_df = df.filter(df("commentBody").isNotNull)
        .withColumn("cleanComment", clean_comment(df("commentBody")))

    //Tokenize are remove stop words
    val regexTokenizer = new RegexTokenizer()
    .setInputCol("cleanComment")
    .setOutputCol("words")
    .setPattern("\\W")
    .setMinTokenLength(3)

    val tokenized_df = regexTokenizer.transform(filtered_df)

    val remover = new StopWordsRemover()
        .setInputCol("words")
        .setOutputCol("filtered_words")

    val removed_df = remover.transform(tokenized_df)

    //Create vocab
    val cvModel: CountVectorizerModel = new CountVectorizer()
        .setInputCol("filtered_words")
        .setOutputCol("features")
        .setVocabSize(5000)
        .setMinDF(5)
        .fit(removed_df)
        
    val json_vocab = cvModel
        .vocabulary
        .toSeq
        .zipWithIndex
        .map {
            case (word, index) => s""""${word}":${index}"""
        }
        .mkString("{\n",",\n","\n}")
        
    val s3 = AmazonS3ClientBuilder.defaultClient()
    s3.putObject(bucket,"data/nyt-features/vocab.json",json_vocab)
        
    //Create the bag-of-words
    val feature_df = cvModel.transform(removed_df)

    val labeled_df = feature_df
        .select("articleID", "commentID", "commentBody", "features")
        .withColumn("labels", lit(0.0))

    //Save a parquet version of the features for Athena
    labeled_df
        .write
        .mode("overwrite")
        .parquet(s"s3://${bucket}/data/nyt-features/labeled_data.parquet")

    //Save a protocol buffer version of features for Sagemaker
    labeled_df
        .coalesce(1)
        .write
        .mode("overwrite")
        .format("sagemaker")
        .option("labelColumnName", "labels")
        .option("featuresColumnName", "features")
        .save(s"s3://${bucket}/data/nyt-record-io/training.rec")
    
    //Sample some comments for testing
    labeled_df
        .sample(false, 0.0001)
        .select("commentBody")
        .coalesce(1)
        .write
        .mode("overwrite")
        .csv(s"s3://${bucket}/data/nyt-features/sampled_data")
  }
}