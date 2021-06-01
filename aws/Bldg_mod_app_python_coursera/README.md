AWS services used: Amazon S3, Amazon API Gateway, Amazon Cognito, AWS Lambda, AWS Step Functions, AWS X-Ray, and Amazon Comprehend.

Lab 1: Create a static  Amazon S3 website with a bucket policy that restricts access to the website via IP Address. The website will be created using the AWS SDK and AWS CLI.

Lab 2: Setup mock backend API using Amazon API Gateway REST APIs. You will setup 3 API endpoints using the AWS SDK and AWS CLI, these endpoints will respond to requests with mocked data. You will test this mock API using the website setup in Lab 1 make AJAX calls to the mock API.

Lab 3: Secure the API that was built in Lab 2 by adding authentication via Amazon Cognito User Pools.

Lab 4: Create AWS Lambda functions to host the backend for your API. You will then configure the secured API built in Lab 3 to trigger to the lambda functions, instead of using mock integrations.

Lab 5: Create an asynchronous state machine using AWS Step Functions for a reporting feature of the API. You will then configure the API to run this state machine when a request hits an API endpoint you built in the previous labs.

Lab 6: Use AWS X-Ray to trace requests through your distributed application. You will also make improvements to your application using various AWS service features like Amazon API Gateway Response Caching, as well as code modifications. Then you will test and view the performance improvements in the AWS X-Ray Console.