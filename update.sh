read -p "Comments:" comments
new_version=$(cat chenile-*-version.txt)
git add .
git commit -m "${new_version}:${comments}"
git push origin main
make tag tag=$new_version
make push-tags

