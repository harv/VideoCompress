# Android Video Compressor
- Fork from https://github.com/yovenny/VideoCompress.git
- Video Compressor based&update in the [**Telegram for Android**](https://github.com/DrKLO/Telegram) [![Telegram for Android](https://raw.githubusercontent.com/lalongooo/VideoCompressor/master/images/ic_launcher.png)](https://github.com/DrKLO/Telegram) app source code.
- Any different model problem can check the source code of  [**Telegram for Android**](https://github.com/DrKLO/Telegram)，cause there are no more tests

## Setup

```groovy
  compile project(':videocompress')
```

## Usage

```java
   //in a new Thread
   boolean isCompressSuccess=MediaController.getInstance().convertVideo(inPath,outPath);
```

## Log
**2019-5-23**
- update to telegram 5.6.1