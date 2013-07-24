#!/bin/sh

# base directory, at top of source tree; replace with absolute path
base=`pwd`

# configure root dir of interesting stuff
root=$base/out/host/linux-x86
export ANDROID_ROOT=$root

# configure bootclasspath
bootpath=$base/out/target/product/generic_x86/system/framework
export BOOTCLASSPATH=$bootpath/core.jar:$bootpath/ext.jar:$bootpath/framework.jar:$bootpath/android.policy.jar:$bootpath/services.jar

export LD_LIBRARY_PATH=$bootpath/lib:$LD_LIBRARY_PATH

# this is where we create the dalvik-cache directory; make sure it exists
export ANDROID_DATA=/tmp/dalvik_$USER
mkdir -p $ANDROID_DATA/dalvik-cache

exec $root/bin/dalvikvm -Xbootclasspath:$BOOTCLASSPATH $@
