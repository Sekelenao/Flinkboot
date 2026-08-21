#!/usr/bin/env bash

cd ".."

echo "==============[DEPENDENCIES]=============="

echo "Running 'mvn versions:display-dependency-updates', please wait..."

mvn versions:display-dependency-updates | grep -Ei ' -> |No dependencies in Dependencies have newer versions'

echo "==============[PLUGINS]=============="

echo "Running 'mvn versions:display-plugin-updates', please wait..."

mvn versions:display-plugin-updates | grep -Ei ' -> |require'