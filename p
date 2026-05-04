[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] java-scheduler                                                     [pom]
[INFO] java-scheduler-api                                                 [jar]
[INFO] java-scheduler-worker                                              [jar]
[INFO] 
[INFO] ----------------------< com.joel:java-scheduler >-----------------------
[INFO] Building java-scheduler 0.0.1-SNAPSHOT                             [1/3]
[INFO]   from pom.xml
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for java-scheduler 0.0.1-SNAPSHOT:
[INFO] 
[INFO] java-scheduler ..................................... FAILURE [  0.001 s]
[INFO] java-scheduler-api ................................. SKIPPED
[INFO] java-scheduler-worker .............................. SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.120 s
[INFO] Finished at: 2026-05-03T10:46:43+09:00
[INFO] ------------------------------------------------------------------------
[ERROR] Unknown lifecycle phase "java-scheduler-api". You must specify a valid lifecycle phase or a goal in the format <plugin-prefix>:<goal> or <plugin-group-id>:<plugin-artifact-id>[:<plugin-version>]:<goal>. Available lifecycle phases are: pre-clean, clean, post-clean, validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy, pre-site, site, post-site, site-deploy. -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/LifecyclePhaseNotFoundException
