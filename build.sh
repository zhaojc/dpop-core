#!/bin/bash

JAVA_HOME=$JAVA_HOME_1_6
export JAVA_HOME
PATH=$JAVA_HOME/bin:$PATH

export MAVEN_HOME=$MAVEN_3_0_4
PATH=$MAVEN_HOME:$PATH
export PATH
		echo "**********************************************"
        echo "use online profile"
        echo "**********************************************"
		
mvn clean
mvn deploy -Dmaven.test.skip=true -Ponline
