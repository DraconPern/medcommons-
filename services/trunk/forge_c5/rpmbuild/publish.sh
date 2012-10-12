if [ -z ${FORGE_REPO} ]; then
	echo "Where to? This should run from build.sh which sets FORGE_REPO.";
	exit;
fi

rm -rf ../medcommons/repostage/* || :
for fn in `find RPMS -type f -name "*.rpm" | tr '\n' ' '`; do
  cp -a $fn ../medcommons/repostage
done &&
createrepo ../medcommons/repostage &&
pushd ${FB_ROOT}/services/${FB_TRUNK}/forge/medcommons/repostage &&
find . -type f -newer .lastpublish_${FORGE_REPO} | sed -e 's|\./\(.*\)|retry s3.py put ${FB_REPO}/${FB_RREV}/${FORGE_REPO}/x86_64/\1 \1|' | sh &&
find . -type f -newer .lastpublish_${FORGE_REPO} | sed -e 's|\./\(.*\)|retry s3.py share ${FB_REPO}/${FB_RREV}/${FORGE_REPO}/x86_64/\1 owner:FULL_CONTROL all:READ|' | sh &&
touch .lastpublish_${FORGE_REPO} &&
popd
