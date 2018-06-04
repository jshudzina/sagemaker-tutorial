# Sage Maker Micro-Service Tutorial

A Tutorial for BigData Tech 2018

## Motivation

This tutorial attempts to add a software engineering perspective to model
development and deployment.  Machine learning models don't only
live in Jupyter notebooks. Often the models are integrated into applications.  
These applications might employ several models to complete a task. For example, a
notional Question & Answer (Q&A) application might leverage several models to answer a
question.

Although the notional Q&A app might leverage a single framework to complete
the task, a monolithic application imposes several constraints.  A monolithic
app restricts researchers to implement a model in a particular language or API.
A monolithic app forces the researchers to deploy the entire application instead of
swapping out a model.  Finally, a monolithic application makes it difficult to
test competing models in parallel (i.e. A/B Testing).

If the notional Q&A app leveraged a micro-service architecture instead of a
monolithic application, then the researchers have more flexibility because the
models are developed and deployed independently.  As a consequence, the
researchers aren't restricted to a single language or model framework.  Additionally,
micro-services are easier to swap out and or run in parallel than a single notebook instance.

![Notional Q&A System](/images/QAMicroServices.png)


## SageMaker Micro-Services

![SageMaker MicroServices](/images/SagemakerDiagram.png)

## Tutorial Overview

Although Amazon provides
[multiple tutorials](https://github.com/awslabs/amazon-sagemaker-examples), the
Amazon  tutorials focus on the model development and not the model's integration
with an application.  This tutorial integrates SageMaker with other services,
like AWS Glue and Lambda.

The tutorial will...
1. Prep NY Times comments as a bag-of-words training set with AWS glue
2. Train a Neural Topic Model (NTM) with Amazon SageMaker's Training Service
3. Deploy the NTM with Amazon SageMaker's Deployment Service
4. Mock a lambda function to call the NTM's end point

## Tutorial Installation

1. Create the ```SageMakerTutorialRole``` via the supplied CloudFormation
  template, [cf-template.yaml](cf-template.yaml).  See detailed instructions [here](RoleSetup.md).
2. Create a SageMaker notebook using the ```SageMakerTutorialRole```. See detailed instructions [here](CreateNotebook.md).


### Notebook Setup

1. Open a terminal window from jupyter...   **open** -> **terminal**
2. In the terminal window ```cd SageMaker```
3. Clone the git repository ```git clone https://github.com/jshudzina/sagemaker-tutorial.git```
