#!/bin/bash
if [[ -z $1 ]]; then
  echo "Usage: $0 version_tag"
  exit 1
fi

git tag -a -m "Tagging version $1" "$1"
git push origin --tags
