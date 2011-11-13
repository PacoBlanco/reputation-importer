#!/bin/bash
python userAccounts.py
java -jar reputationWiki.jar
python putUserReputation.py
