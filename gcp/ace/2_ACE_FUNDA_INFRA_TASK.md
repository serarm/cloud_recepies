
# Perform Foundational Infrastructure Tasks in Google Cloud

## Task1: Create a bucket for storing the photographs.
```bash
gsutil mb gs://YOUR-BUCKET-NAME/
```

## Task2: Create a Pub/Sub topic that will be used by a Cloud Function you create.

1. Create topic
```bash
gcloud pubsub topics create myTopic
gcloud pubsub topics list
```

2. Create subscriptions
```bash
gcloud  pubsub subscriptions create --topic myTopic mySubscription
gcloud pubsub topics list-subscriptions myTopic
```

3. Sample publishing and subscribing to the topic

```bash
gcloud pubsub topics publish myTopic --message "Publisher likes to eat <FOOD>"
gcloud pubsub subscriptions pull mySubscription --auto-ack --limit=3
```


## Task3: Create a Cloud Function.
+ Create a Cloud Function that executes every time an object is created in the bucket you created in task 1. 
+ The function is written in Node.js 10. Make sure you set the Entry point (Function to execute) to thumbnail and Trigger to Cloud Storage.
+ In line 15 of index.js replace the text REPLACE_WITH_YOUR_TOPIC ID with the Topic ID you created in task 2.
```bash
mkdir thumbnail
cd thumbnail
nano index.js
gcloud functions deploy thumbnail \
  --trigger-bucket $BUCKET_NAME \
  --runtime nodejs10 \
  --allow-unauthenticated 
  --region us-east1
gcloud functions describe thumbnail
```
+ Check with the image at [link](https://storage.googleapis.com/cloud-training/gsp315/map.jpg) and upload it in the bucket

![Sample](img/2_sample_image.jpg "Sample")
**Ensure that the extension is jpg not jpeg **

<details>
<summary>index.js</summary>

```javascript
/* globals exports, require */
//jshint strict: false
//jshint esversion: 6
"use strict";
const crc32 = require("fast-crc32c");
const gcs = require("@google-cloud/storage")();
const PubSub = require("@google-cloud/pubsub");
const imagemagick = require("imagemagick-stream");

exports.thumbnail = (event, context) => {
  const fileName = event.name;
  const bucketName = event.bucket;
  const size = "64x64"
  const bucket = gcs.bucket(bucketName);
  const topicName = "REPLACE_WITH_YOUR_TOPIC ID";
  const pubsub = new PubSub();
  if ( fileName.search("64x64_thumbnail") == -1 ){
    // doesn't have a thumbnail, get the filename extension
    var filename_split = fileName.split('.');
    var filename_ext = filename_split[filename_split.length - 1];
    var filename_without_ext = fileName.substring(0, fileName.length - filename_ext.length );
    if (filename_ext.toLowerCase() == 'png' || filename_ext.toLowerCase() == 'jpg'){
      // only support png and jpg at this point
      console.log(`Processing Original: gs://${bucketName}/${fileName}`);
      const gcsObject = bucket.file(fileName);
      let newFilename = filename_without_ext + size + '_thumbnail.' + filename_ext;
      let gcsNewObject = bucket.file(newFilename);
      let srcStream = gcsObject.createReadStream();
      let dstStream = gcsNewObject.createWriteStream();
      let resize = imagemagick().resize(size).quality(90);
      srcStream.pipe(resize).pipe(dstStream);
      return new Promise((resolve, reject) => {
        dstStream
          .on("error", (err) => {
            console.log(`Error: ${err}`);
            reject(err);
          })
          .on("finish", () => {
            console.log(`Success: ${fileName} → ${newFilename}`);
              // set the content-type
              gcsNewObject.setMetadata(
              {
                contentType: 'image/'+ filename_ext.toLowerCase()
              }, function(err, apiResponse) {});
              pubsub
                .topic(topicName)
                .publisher()
                .publish(Buffer.from(newFilename))
                .then(messageId => {
                  console.log(`Message ${messageId} published.`);
                })
                .catch(err => {
                  console.error('ERROR:', err);
                });

          });
      });
    }
    else {
      console.log(`gs://${bucketName}/${fileName} is not an image I can handle`);
    }
  }
  else {
    console.log(`gs://${bucketName}/${fileName} already has a thumbnail`);
  }
};
```

</details>

<details>
<summary>package.json</summary>

```javascript
{
  "name": "thumbnails",
  "version": "1.0.0",
  "description": "Create Thumbnail of uploaded image",
  "scripts": {
    "start": "node index.js"
  },
  "dependencies": {
    "@google-cloud/storage": "1.5.1",
    "@google-cloud/pubsub": "^0.18.0",
    "fast-crc32c": "1.0.4",
    "imagemagick-stream": "4.1.1"
  },
  "devDependencies": {},
  "engines": {
    "node": ">=4.3.2"
  }
}
```

</details>

## Task4: Remove the previous cloud engineer’s access from the memories project.

> Navigation menu > IAM & Admin > IAM
