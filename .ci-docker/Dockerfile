FROM openjdk:8-jdk
RUN apt-get -y update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y apt-utils
RUN DEBIAN_FRONTEND=noninteractive apt-get dist-upgrade -y
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y wget tar unzip lib32stdc++6 lib32z1 git

ENV ANDROID_COMPILE_SDK "28"
ENV ANDROID_BUILD_TOOLS "28.0.3"
ENV ANDROID_SDK_TOOLS "4333796"

WORKDIR /opt/
RUN wget --quiet --output-document=android-sdk.zip https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_TOOLS}.zip
RUN unzip -q -d android-sdk-linux android-sdk.zip
RUN echo y | android-sdk-linux/tools/bin/sdkmanager "platforms;android-${ANDROID_COMPILE_SDK}" >/dev/null
RUN echo y | android-sdk-linux/tools/bin/sdkmanager "platform-tools" >/dev/null
RUN echo y | android-sdk-linux/tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS}" >/dev/null
RUN yes | android-sdk-linux/tools/bin/sdkmanager --licenses

WORKDIR /tmp/
RUN git clone --depth=1 https://gitlab.com/tom79/mastalab
WORKDIR /tmp/mastalab
RUN chmod +x gradlew
RUN ANDROID_HOME=/opt/android-sdk-linux PATH=$PATH:/opt/android-sdk-linux/platform-tools/ ./gradlew tasks
RUN ANDROID_HOME=/opt/android-sdk-linux PATH=$PATH:/opt/android-sdk-linux/platform-tools/ ./gradlew assembleDebug
RUN ANDROID_HOME=/opt/android-sdk-linux PATH=$PATH:/opt/android-sdk-linux/platform-tools/ ./gradlew --stacktrace -Pci --console=plain :app:testFdroidDebugUnitTest
RUN ANDROID_HOME=/opt/android-sdk-linux PATH=$PATH:/opt/android-sdk-linux/platform-tools/ ./gradlew --stacktrace -Pci --console=plain :app:lintFdroidDebug -PbuildDir=lint
