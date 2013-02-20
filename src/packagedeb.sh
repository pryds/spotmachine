#!/bin/sh

PACKAGE_NAME="spotmachine"
PACKAGE_VERSION="0.3"
SOURCE_DIR=$PWD
TEMP_DIR="/tmp"

mkdir -p $TEMP_DIR/debian/DEBIAN
mkdir -p $TEMP_DIR/debian/lib
mkdir -p $TEMP_DIR/debian/bin
mkdir -p $TEMP_DIR/debian/usr/share/applications
mkdir -p $TEMP_DIR/debian/usr/share/doc/$PACKAGE_NAME

cp debian/control $TEMP_DIR/debian/DEBIAN/
cp debian/spotmachine.desktop $TEMP_DIR/debian/usr/share/applications/
cp debian/copyright $TEMP_DIR/debian/usr/share/doc/$PACKAGE_NAME/
cp -r ../bin/ $TEMP_DIR/debian/lib/$PACKAGE_NAME
gzip -9c $TEMP_DIR/debian/lib/$PACKAGE_NAME/NEWS > $TEMP_DIR/debian/usr/share/doc/$PACKAGE_NAME/changelog.gz
mv $TEMP_DIR/debian/lib/$PACKAGE_NAME/resources/spotmachine.svg $TEMP_DIR/debian/usr/share/doc/$PACKAGE_NAME/
chmod 644 $TEMP_DIR/debian/usr/share/doc/$PACKAGE_NAME/spotmachine.svg
rm -r $TEMP_DIR/debian/lib/$PACKAGE_NAME/debian
rm $TEMP_DIR/debian/lib/$PACKAGE_NAME/COPYING
rm $TEMP_DIR/debian/lib/$PACKAGE_NAME/packagedeb.sh

echo '#!/bin/sh' > $TEMP_DIR/debian/bin/spotmachine
echo "export CLASSPATH=$CLASSPATH:/lib/$PACKAGE_NAME" >> $TEMP_DIR/debian/bin/spotmachine
echo "java main/SpotMachine $1" >> $TEMP_DIR/debian/bin/spotmachine
chmod 755 $TEMP_DIR/debian/bin/spotmachine

chown -R root $TEMP_DIR/debian/
chgrp -R root $TEMP_DIR/debian/

cd $TEMP_DIR/
dpkg --build debian
mv debian.deb $SOURCE_DIR/$PACKAGE_NAME-$PACKAGE_VERSION.deb

rm -r $TEMP_DIR/debian
