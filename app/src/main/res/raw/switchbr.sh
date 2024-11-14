#!/system/bin/sh
#
# Copyright 2024 david42069
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# "*******************************"
# "Dualboot script for any device"
# "--Made by david42069"
# "--And ExtremeXT"
# "*******************************"

DATA_PATH=$(dumpsys package com.david42069.dualboothelper | grep -i dataDir | cut -d'=' -f2-)
PARTED_PATH="$DATA_PATH/files/parted"
JQ_PATH="$DATA_PATH/files/jq"

#function to get a property from a partition
get_partition_property() {
    local partition_name="$1"
    local property="$2"
    echo "$output" | $JQ_PATH -r '.disk.partitions[] | select(.name == "'"$partition_name"'") | '"$property" | sed 's/B$//'
}



#test if device uses caps or not based on boot naming
use_caps=0 
failsafe=0 

#test if device uses caps or not based on boot naming 
if ls -l /dev/block/by-name/ | grep -q 'boot'; 
then 
use_caps=1 
fi 
if ls -l /dev/block/by-name/ | grep -q 'BOOT'; 
then 
use_caps=2 
fi 
if [ "$use_caps" -eq 0 ]  
then 
#attempt a second, not so efficient way of detecting device type in case of symlimk by-name fail 
failsafe=1 
if ls /dev/block/ | grep -q 'mmcblk0p11'; 
then  
if ls /dev/block/ | grep -q 'mmcblk0p12'; 
then  
DISK=/dev/block/mmcblk0 
fi 
fi 
 
if ls /dev/block/ | grep -q 'sda11'; 
then  
if ls /dev/block/ | grep -q 'sda12'; 
then  
DISK=/dev/block/sda 
fi 
fi 
if ls /dev/block/ | grep -q 'sdc11'; 
then  
if ls /dev/block/ | grep -q 'sdc12'; 
then  
DISK=/dev/block/sdc 
fi 
fi 
output=$($PARTED_PATH $DISK -s -j unit B print 2>/dev/null) 
capitalboot=$(echo "$output" | $JQ_PATH -r '.disk.partitions[] | select(.name == "BOOT") | .number') 
noncapitalboot=$(echo "$output" | $JQ_PATH -r '.disk.partitions[] | select(.name == "boot") | .number') 
if [ "$capitalboot" -ne 0 ] 
then 
use_caps=2 
fi 
if [ "$noncapitalboot" -ne 0 ] 
then 
use_caps=1 
fi 
fi 
if [ "$use_caps" -eq 0 ] 
then 
#exit if device is not compatible  
exit 1; 
fi 

#Create partition names according to device 
if [ "$use_caps" -eq 1 ]
then
    system_b_name="system_b"
    product_b_name="product_b"
    vendor_b_name="vendor_b"
    boot_b_name="boot_b"
    efs_b_name="efs_b"
    cache_b_name="cache_b"
    odm_b_name="odm_b"
    dtb_b_name="dtb_b"
    dtbo_b_name="dtbo_b" 
    recovery_b_name="recovery_b"
    userdata_b_name="userdata_b"
    userdata_a_name="userdata"
    boot_a_name="boot"
    vendor_a_name="vendor"
    product_a_name="product"
    system_a_name="system"
    recovery_a_name="recovery"
    efs_a_name="efs"
    cache_a_name="cache"
    odm_a_name="odm"
    dtb_a_name="dtb"
    dtbo_a_name="dtbo"
fi

if [ "$use_caps" -eq 2 ]
then
    system_b_name="SYSTEM_B"
    product_b_name="PRODUCT_B"
    vendor_b_name="VENDOR_B"
    boot_b_name="BOOT_B"
    efs_b_name="EFS_B"
    cache_b_name="CACHE_B"
    recovery_b_name="RECOVERY_B"
    userdata_b_name="USERDATA_B"
    odm_b_name="ODM_B"
    dtb_b_name="DTB_B"
    dtbo_b_name="DTBO_B" 
    userdata_a_name="USERDATA"
    boot_a_name="BOOT"
    vendor_a_name="VENDOR"
    product_a_name="PRODUCT"
    system_a_name="SYSTEM"
    recovery_a_name="RECOVERY"
    efs_a_name="EFS"
    cache_a_name="CACHE"
    odm_a_name="ODM"
    dtb_a_name="DTB"
    dtbo_a_name="DTBO"
fi

#get disk path
if [ "$failsafe" -eq 0 ] 
then 
DISK=$(echo "$(ls -l /dev/block/by-name/${boot_a_name})" | sed -r 's|.*-> (.*)|\1|') 
DISK=$(echo "$DISK" | sed -r 's|p?[0-9]+$||') 
fi 
#disk small is only the name of the block for example sda sdc mmcblk0
DISK_SMALL=$(echo "$DISK" | sed -r 's|.*/([^/]+)|\1|')
output=$($PARTED_PATH $DISK -s -j unit B print 2>/dev/null)
#gather partition numbers
system_b_num=$(get_partition_property "$system_b_name" ".number")
vendor_b_num=$(get_partition_property "$vendor_b_name" ".number")
product_b_num=$(get_partition_property "$product_b_name" ".number")
boot_b_num=$(get_partition_property "$boot_b_name" ".number")
recovery_b_num=$(get_partition_property "$recovery_b_name" ".number")
efs_b_num=$(get_partition_property "$efs_b_name" ".number")
cache_b_num=$(get_partition_property "$cache_b_name" ".number")
dtb_b_num=$(get_partition_property "$dtb_b_name" ".number")
dtbo_b_num=$(get_partition_property "$dtbo_b_name" ".number")
odm_b_num=$(get_partition_property "$odm_b_name" ".number")
userdata_b_num=$(get_partition_property "$userdata_b_name" ".number")
super_b_num=$(get_partition_property "super_b" ".number")
prism_b_num=$(get_partition_property "prism_b" ".number")
optics_b_num=$(get_partition_property "optics_b" ".number")

system_a_num=$(get_partition_property "$system_a_name" ".number")
vendor_a_num=$(get_partition_property "$vendor_a_name" ".number")
product_a_num=$(get_partition_property "$product_a_name" ".number")
boot_a_num=$(get_partition_property "$boot_a_name" ".number")
recovery_a_num=$(get_partition_property "$recovery_a_name" ".number")
efs_a_num=$(get_partition_property "$efs_a_name" ".number")
cache_a_num=$(get_partition_property "$cache_a_name" ".number")
dtb_a_num=$(get_partition_property "$dtb_a_name" ".number")
dtbo_a_num=$(get_partition_property "$dtbo_a_name" ".number")
odm_a_num=$(get_partition_property "$odm_a_name" ".number")
userdata_a_num=$(get_partition_property "$userdata_a_name" ".number")
super_a_num=$(get_partition_property "super" ".number")
prism_a_num=$(get_partition_property "prism" ".number")
optics_a_num=$(get_partition_property "optics" ".number")
if [ $userdata_a_num -lt $userdata_b_num ]
then
if [ "$super_b_num" -ne 0 ]
then
$PARTED_PATH $DISK -s name $super_a_num super_b
$PARTED_PATH $DISK -s name $super_b_num super
fi
if [ "$prism_b_num" -ne 0 ]
then
$PARTED_PATH $DISK -s name $prism_a_num prism_b
$PARTED_PATH $DISK -s name $prism_b_num prism
fi
if [ "$optics_b_num" -ne 0 ]
then
$PARTED_PATH $DISK -s name $optics_a_num optics_b
$PARTED_PATH $DISK -s name $optics_b_num optics
fi
if [ "$system_b_num" -ne 0 ]
then
$PARTED_PATH $DISK -s name $system_a_num $system_b_name
$PARTED_PATH $DISK -s name $system_b_num $system_a_name
fi
if [ "$vendor_b_num" -ne 0 ]
then
$PARTED_PATH $DISK -s name $vendor_a_num $vendor_b_name
$PARTED_PATH $DISK -s name $vendor_b_num $vendor_a_name
fi
if [ "$product_b_num" -ne 0 ]
then
$PARTED_PATH $DISK -s name $product_a_num $product_b_name
$PARTED_PATH $DISK -s name $product_b_num $product_a_name
fi
if [ "$odm_b_num" -ne 0 ]
then
$PARTED_PATH $DISK -s name $odm_a_num $odm_b_name
$PARTED_PATH $DISK -s name $odm_b_num $odm_a_name
fi
if [ "$cache_b_num" -ne 0 ]
then
$PARTED_PATH $DISK -s name $cache_a_num $cache_b_name
$PARTED_PATH $DISK -s name $cache_b_num $cache_a_name
fi
if [ "$efs_b_num" -ne 0 ]
then
$PARTED_PATH $DISK -s name $efs_a_num $efs_b_name
$PARTED_PATH $DISK -s name $efs_b_num $efs_a_name
fi
if [ "$userdata_b_num" -ne 0 ]
then
$PARTED_PATH $DISK -s name $userdata_a_num $userdata_b_name
$PARTED_PATH $DISK -s name $userdata_b_num $userdata_a_name
fi
#dd boot/recovery/dtb/dtbo
if [ "$boot_b_num" -ne 0 ]
then
dd if=$DISK$boot_b_num of=$DATA_PATH/files/boot.img
dd if=$DISK$boot_a_num of=$DISK$boot_b_num
dd if=$DATA_PATH/files/boot.img of=$DISK$boot_a_num
fi
if [ "$recovery_b_num" -ne 0 ]
then
dd if=$DISK$recovery_b_num of=$DATA_PATH/files/recovery.img
dd if=$DISK$recovery_a_num of=$DISK$recovery_b_num
dd if=$DATA_PATH/files/recovery.img of=$DISK$recovery_a_num
fi
if [ "$dtb_b_num" -ne 0 ]
then
dd if=$DISK$dtb_b_num of=$DATA_PATH/files/dtb.img
dd if=$DISK$dtb_a_num of=$DISK$dtb_b_num
dd if=$DATA_PATH/files/dtb.img of=$DISK$dtb_a_num
fi
if [ "$dtbo_b_num" -ne 0 ]
then
dd if=$DISK$dtbo_b_num of=$DATA_PATH/files/dtbo.img
dd if=$DISK$dtbo_a_num of=$DISK$dtbo_b_num
dd if=$DATA_PATH/files/dtbo.img of=$DISK$dtbo_a_num
fi
fi
reboot recovery
