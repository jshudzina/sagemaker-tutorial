import com.amazonaws.services.glue.GlueContext
import com.amazonaws.services.glue.util.GlueArgParser
import com.amazonaws.services.glue.util.Job
import org.apache.spark.SparkContext

import org.apache.spark.sql.functions._

import org.apache.spark.ml.feature.{RegexTokenizer, StopWordsRemover, CountVectorizer, CountVectorizerModel}

import scala.collection.JavaConverters._

object Tokenizer {
  def main(sysArgs: Array[String]) {
    val sc: SparkContext = new SparkContext()
    val glueContext: GlueContext = new GlueContext(sc)
    // @params: [JOB_NAME]
    val args = GlueArgParser.getResolvedOptions(sysArgs, Seq("JOB_NAME").toArray)
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
        .csv("s3://sagemaker-us-east-1-228889150161/data/nyt-comments/CommentsMarch2018.csv")

    val filtered_df = df.filter(df("commentBody").isNotNull)

    val regexTokenizer = new RegexTokenizer()
    .setInputCol("commentBody")
    .setOutputCol("words")
    .setPattern("\\W")
    .setMinTokenLength(3)

    val tokenized_df = regexTokenizer.transform(filtered_df)

    val remover = new StopWordsRemover()
        .setInputCol("words")
        .setOutputCol("filtered_words")

    val removed_df = remover.transform(tokenized_df)

    val cvModel: CountVectorizerModel = new CountVectorizer()
        .setInputCol("filtered_words")
        .setOutputCol("features")
        .setVocabSize(1000)
        .setMinDF(5)
        .fit(removed_df)

    val feature_df = cvModel.transform(removed_df)

    val labeled_df = feature_df
        .select("articleID", "commentID", "commentBody", "features")
        .withColumn("labels", lit(0.0))

    labeled_df.show()

    labeled_df
        .write
        .mode("overwrite")
        .parquet("s3://sagemaker-us-east-1-228889150161/data/nyt-features/labeled_data.parquet")

    labeled_df
        .coalesce(1)
        .write
        .mode("overwrite")
        .format("sagemaker")
        .option("labelColumnName", "labels")
        .option("featuresColumnName", "features")
        .save("s3://sagemaker-us-east-1-228889150161/data/nyt-record-io/training.rec")
  }
}
