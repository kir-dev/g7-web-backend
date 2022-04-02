CMSch web backend and frontend monorepo
===

<a href="https://cmsch.vercel.app"><img src="https://therealsujitk-vercel-badge.vercel.app/?app=cmsch&style=for-the-badge"></a>

## Build docker

```bash
  ./gradlew clean build
  docker build -t cmsch .
```

## Run

```bash
  ./gradlew bootRun --args='--spring.profiles.include=test,internal,golyakorte2022'
```

## Enable profileing

```bash
  ./gradlew -Dorg.gradle.jvmargs="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.rmi.port=9010 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -XX:+UseSerialGC" clean bootRun --args='--spring.profiles.include=test,internal'
```

## Publish

Use your authsch details for docker login. Tag `rc` for staging (release candidate) and tag release for release.

```bash
  docker login harbor.sch.bme.hu
  
  # Release candidate
  docker image tag cmsch:latest harbor.sch.bme.hu/org-golyakorte/cmsch:rc
  docker image push harbor.sch.bme.hu/org-golyakorte/cmsch:rc
  
  # Release (you can use versions like ':major.minor.build' as well)
  docker image tag cmsch:latest harbor.sch.bme.hu/org-golyakorte/cmsch:release
  docker image push harbor.sch.bme.hu/org-golyakorte/cmsch:release
```

## Run (you can start here)

For develpment:

```bash
  docker run --rm -p 8080:80 \
        -e AUTHSCH_CLIENT_ID=20_CHARS \
        -e AUTHSCH_CLIENT_KEY=80_CHARS \
        -e PROFILE_SALT=RANDOM_STRING \
        -e SYSADMINS=YOUR_AUTH_SCH_UUID \
        cmsch
```

or from the registry: **YOU MIGHT PROBABLY WANT TO START WITH THIS**

```bash
  docker pull harbor.sch.bme.hu/org-golyakorte/cmsch
  docker run --rm -p 8080:80 \
        -e AUTHSCH_CLIENT_ID=20_CHARS \
        -e AUTHSCH_CLIENT_KEY=80_CHARS \
        -e PROFILE_SALT=RANDOM_STRING \
        -e SYSADMINS=YOUR_AUTH_SCH_UUID \
        harbor.sch.bme.hu/org-golyakorte/cmsch
```

## Where to start?

- Api docs: BASE_URL/swagger-ui.html
- Admin UI: BASE_URL/admin/control/basics
- API: BASE_URL/api/... (see swagger for more)

## Required apps

You must install:

- Node v16
- Yarn v1.22.17
- IDEA or at least Gradle

## Application local properties

Create an application-local.properties file in the `src/main/resources/config` folder, 
and fill the file with these configurations (using your credentials): 

```properties
authsch.client-identifier=<insert the shorter key>
authsch.client-key=<insert the long key>
cmsch.sysadmins=<your pekId>
cmsch.website-default-url=http://<your ip>:8080/
logging.level.web=DEBUG
```

Your pekId can be found in the console log of the Spring app when signing in with AuthSCH. The `cmsch.website-default-url`
property's IP address needs to be either `localhost` or the IP of your current device running your Spring app on your network.

Once created, edit the `CMSchApplication` Run Configuration's Spring Boot Active Profiles to use (see image down below)

- `local,test` if you want test data in the database also
- `local` if you don't

![runconfig](.readme-files/runconfig.png)

## Sponsors

<a href="https://vercel.com?utm_source=kir-dev&utm_campaign=oss"><img src="client/public/img/powered-by-vercel.svg" height="46" /></a>
