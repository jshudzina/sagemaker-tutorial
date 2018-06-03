# Role Step Up

Before creating the SageMaker notebook, this tutorial requires a role that
grants permission to both SageMaker and Glue.  This instructions cover the
role creation via a cloud formation template.

1. Open the AWS Console.
2. Select Services -> CloudFormation (hint: use the search box).
3. Click ```Create Stack```
4. Select **Upload a template to S3** and choose [cf-template.yaml](cf-template.yaml)
  ![Upload Template](/images/CreateStack01.png)
5. Type in **sagemaker-tutorial** as the stack name and click next.
  ![Name Stack](/images/CreateStack02.png)
6. Click next on the Advanced Setting Page
7. Acknowledge that __AWS CloudFormation might create IAM resources with custom names__ and click ```Create```
8. The **sagemaker-tutorial** will display the status __CREATE COMPLETE__ once done.
  ![Stack Ready](/images/CreateStack04.png)
