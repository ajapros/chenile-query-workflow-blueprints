new_version=$(cat chenile-*-version.txt)
git add .
git commit -m "${new_version}"
git push origin main
make tag tag=$new_version
make push-tags

