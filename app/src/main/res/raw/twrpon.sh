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

# Get the current user ID
USER_ID=$(am get-current-user)
DATA_PATH=$(dumpsys package com.david42069.dualboothelper | grep -i dataDir | grep "/data/user/$USER_ID/" | cut -d'=' -f2-)
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
    userdata_b_name="userdata_b"
    userdata_a_name="userdata"
    boot_a_name="boot"
fi

if [ "$use_caps" -eq 2 ]
then
    userdata_b_name="USERDATA_B"
    userdata_a_name="USERDATA"
    boot_a_name="BOOT"
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
userdata_b_num=$(get_partition_property "$userdata_b_name" ".number")
userdata_a_num=$(get_partition_property "$userdata_a_name" ".number")
if [ $userdata_a_num -lt $userdata_b_num ]
then
mkdir -p /sdcard/TWRP/theme/
unzip $DATA_PATH/files/slota.zip -d /sdcard/TWRP/theme
exit 0
fi
if [ $userdata_a_num -gt $userdata_b_num ]
then
mkdir -p /sdcard/TWRP/theme/
unzip $DATA_PATH/files/slotb.zip -d /sdcard/TWRP/theme
exit 0
fi
