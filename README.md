# jfix-project-template
Template for new gradle project 

#### Build
##### Deploy to remote maven repository:  
```
gradle publishAllPublicationsToMavenRepository
```
Required properties:
```
repositoryUser= 
repositoryPassword=
repositoryUrl=
```

##### Deploy to local maven repository:  
```
gradle publishToMavenLocal
```

##### Deploy to Maven Central repository:
```
gradle publishToSonatype
```
Required properties:
```
repositoryUser=
repositoryPassword=
signingKeyId=
signingPassword=
```

##### Update project build script and dependencies from project template
```
gradle updateProjectTemplate
```
