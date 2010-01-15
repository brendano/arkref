set -x
#dangerous
./build.sh && rm -rf build
svn status
rm -rf .svn
