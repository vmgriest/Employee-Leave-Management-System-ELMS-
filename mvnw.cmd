@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0") ELSE SET "BASE_DIR=%__MVNW_ARG0_NAME__%"

@SET MAVEN_PROJECTBASEDIR=%BASE_DIR%
@IF NOT "%MAVEN_BASEDIR%"=="" SET MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%

@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@SET DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.1/maven-wrapper-3.3.1.jar"

@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    @IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

@IF EXIST %WRAPPER_JAR% (
    @IF "%MVNW_VERBOSE%"=="true" @ECHO Found %WRAPPER_JAR%
) ELSE (
    @IF NOT "%MVNW_REPOURL%"=="" SET DOWNLOAD_URL="%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.3.1/maven-wrapper-3.3.1.jar"

    @IF "%MVNW_VERBOSE%"=="true" (
        @ECHO Downloading from: %DOWNLOAD_URL%
    )

    @powershell -Command "&{"^
        "$webclient = new-object System.Net.WebClient;"^
        "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
        "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
        "}"^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%DOWNLOAD_URL%', '%WRAPPER_JAR%')"^
        "}"
    @IF "%MVNW_VERBOSE%"=="true" (
        @ECHO Finished downloading %WRAPPER_JAR%
    )
)
@REM End of download

@IF "%JAVA_HOME%"=="" (
    @SET JAVA_EXE=java.exe
) ELSE (
    @SET JAVA_EXE=%JAVA_HOME%/bin/java.exe
)

@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    @IF "%%A"=="distributionUrl" SET DISTRIBUTION_URL=%%B
)

@SET MAVEN_DISTRIBUTION_NAME=apache-maven-3.9.6
@SET MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\%MAVEN_DISTRIBUTION_NAME%

@IF NOT EXIST "%MAVEN_HOME%" (
    @IF "%MVNW_VERBOSE%"=="true" @ECHO Downloading Maven distribution...
    @powershell -Command "&{"^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;"^
        "$url = '%DISTRIBUTION_URL%';"^
        "$dest = '%USERPROFILE%\.m2\wrapper\dists\%MAVEN_DISTRIBUTION_NAME%-bin.zip';"^
        "$null = New-Item -ItemType Directory -Force '%USERPROFILE%\.m2\wrapper\dists';"^
        "$webclient = New-Object System.Net.WebClient;"^
        "$webclient.DownloadFile($url, $dest);"^
        "Expand-Archive -Path $dest -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force;"^
        "Remove-Item $dest"^
        "}"
)

@SET MVN_CMD=%USERPROFILE%\.m2\wrapper\dists\%MAVEN_DISTRIBUTION_NAME%\bin\mvn.cmd
@IF NOT EXIST "%MVN_CMD%" (
    @echo ERROR: Maven distribution not found at %MVN_CMD%
    @echo Please install Maven manually from https://maven.apache.org/download.cgi
    @exit /b 1
)

@"%MVN_CMD%" %*
