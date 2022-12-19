#!/usr/bin/env bash

export ANTORA_CACHE_DIR=.antora-cache-dir
export ANTORA_TARGET_SITE=antora/target/site

PLAYBOOK_FILE=antora/playbooks/site.yml
while getopts 'ecf:aksxyh' opt
do
  case $opt in
    e) export SKIP_EXAMPLES=true ;;
    c) export SKIP_CONFIGS=true ;;
    f) PLAYBOOK_FILE=$OPTARG ;;
    a) export SKIP_GENERATION=true ;;
    k) export SKIP_STALE_EXAMPLE_CHECK=true ;;
    s) export SKIP_SERVE=true ;;
    x) export SKIP_CLEAR_CACHE=true ;;
    y) export SKIP_CLEAR_PREVIOUS=true ;;
    h) echo ""
       echo "preview.sh options:"
       echo "  -e skip examples"
       echo "  -k skip stale example check"
       echo "  -c skip config doc generation"
       echo "  -a skip Antora generation"
       echo "  -s skip serving generated site"
       echo "  -x skip clear Antora cache"
       echo "  -y skip clear any previous build site"
       echo "  -f antora/playbooks/site-xxx.yml"
       exit 1
       ;;
    *) echo "unknown option $opt - aborting" >&2
       exit 1
      ;;
  esac
done

if [[ "$SKIP_CLEAR_CACHE" == "true" ]]; then
  echo "skipping clearing the Antora cache"
else
  rm -rf $ANTORA_CACHE_DIR
fi

if [[ "$SKIP_CLEAR_PREVIOUS" == "true" ]]; then
  echo "skipping clearing any previous build site"
else
  rm -rf $ANTORA_TARGET_SITE
fi

if [ ! -f $PLAYBOOK_FILE ]; then
  echo "no such file $PLAYBOOK_FILE" >&2
  exit 1
fi

export ANTORA_CMD=antora

SECONDS=0
echo "\$PLAYBOOK_FILE = $PLAYBOOK_FILE"
sh build-site.sh $PLAYBOOK_FILE || exit 1


if [[ "$SKIP_SERVE" == "true" ]]; then
  echo "skipping serving"
else
  echo ""
  echo "http://localhost:5000"
  echo ""

  serve -S -p 5000 $ANTORA_TARGET_SITE
fi
