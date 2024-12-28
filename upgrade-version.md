# How to upgrade chenile version
These instructions are for Chenile maintainers to upgrade the version of chenile to a new version.
First of all new versions must only be specified in chenile-parent and not here. We need to upgrade this to
the latest version of chenile parent. 
Version of this repo is changed in chenile-parent by changing the appropriate maven property 
(chenile.query.workflow.blueprints.version) in the pom.xml of chenile-parent.
Let us say chenile-parent is at 2.0.6 and this repo is in 2.0.3. When we update the version in chenile-parent, 
we need to make the changes to the following properties in the pom at chenile-parent:
Bump up the version of chenile-parent from 2.0.6 to 2.0.7
Bump up the version of this pom by changing its corresponding property(chenile.query.workflow.blueprints.version) to 2.0.7
(notice that we skipped a couple of versions for this pom but that is ok)
After that we will have to change the version of this pom by upgrading the version of chenile-parent to 2.0.6

0. Make sure that everything builds good with "make build" 
1. Edit chenile-query-workflow-blueprints-version.txt and pom.xml and replace existing  versions with 2.0.6
2. git add .; git commit -m "Bump up to 2.0.6" ; git push origin main
3. make tag tag=2.0.6 # this wil create the tag
4. make build # All the builds in local maven repo will have the latest version now.
5. passphrase="<secret phrase>" make deploy  # this will deploy to Maven Central
6. make push-tags # pushes the newly created tag into origin
7. make list-local-tags # ensures that the new tag is there locally
8. make list-origin-tags # ensures that the new tag is there in origin.
9. Edit chenile-version.txt and pom.xml to reflect the next snapshot
10. make build once again
11. Publish the snapshot as well and ask your current developers to rely on the snapshot while your non current developers will rely on the latest published version.
12. Don't forget to edit the config/setenv.sh in chenile-gen repo.
13. Finally, if you want you can upgrade the chenile-samples to rely on the latest version of chenile.
