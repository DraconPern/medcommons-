if [ -z ${FORGE_REPO} ]; then
	echo "Where to? This should run from build.sh which sets FORGE_REPO.";
	exit;
fi

rm -rf ../medcommons/i386/* || :
for fn in `find RPMS -type f -name "*.rpm" | tr '\n' ' '`; do
  cp -a $fn ../medcommons/i386
done &&
createrepo ../medcommons/i386 &&
pushd ${FB_ROOT}/services/${FB_TRUNK}/forge/medcommons/i386 &&
find . -type f -newer .lastpublish_${FORGE_REPO} | sed -e 's|\./\(.*\)|retry s3.py put ${FB_REPO}/${FB_RREV}/${FORGE_REPO}/i386/\1 \1|' | sh &&
find . -type f -newer .lastpublish_${FORGE_REPO} | sed -e 's|\./\(.*\)|retry s3.py share ${FB_REPO}/${FB_RREV}/${FORGE_REPO}/i386/\1 owner:FULL_CONTROL all:READ|' | sh &&
touch .lastpublish_${FORGE_REPO} &&
popd
