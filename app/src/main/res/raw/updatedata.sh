
#!/sbin/sh
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

PARTED_PATH="/data/data/com.david42069.dualboothelper/files/parted"
JQ_PATH="/data/data/com.david42069.dualboothelper/files/jq"
chmod 755 /data/data/com.david42069.dualboothelper/files/*
none=0
mkdir -p /cache/dualboot/database/
cp -a /cache/dualboot/database/. ~/data/data/com.david42069.dualboothelper/files/
#use path according if device uses EMMC/EMMC5/UFS
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
if [ "$use_caps" -eq 1 ]
then
efs_a_name="efs"
efs_b_name="efs_b"
userdata_b_name="userdata_b"
userdata_a_name="userdata"
fi
if [ "$use_caps" -eq 2 ]
then
efs_a_name="EFS"
efs_b_name="EFS_B"
userdata_b_name="USERDATA_B"
userdata_a_name="USERDATA"
fi
if [ "$failsafe" -eq 0 ] 
then
DISK=$(echo "$(ls -l /dev/block/by-name/${userdata_a_name})" | sed -r 's|.*-> (.*)|\1|') 
DISK=$(echo "$DISK" | sed -r 's|p?[0-9]+$||') 
fi
output=$($PARTED_PATH $DISK -s -j unit B print 2>/dev/null) 
#determine if dualboot is or not installed
userdata_b_num=$(echo "$output" | $JQ_PATH -r '.disk.partitions[] | select(.name == "'"$userdata_b_name"'") | .number')
userdata_a_num=$(echo "$output" | $JQ_PATH -r '.disk.partitions[] | select(.name == "'"$userdata_a_name"'") | .number')
efs_b_num=$(echo "$output" | $JQ_PATH -r '.disk.partitions[] | select(.name == "'"$efs_b_name"'") | .number')
BUILD_NUMBER=$(getprop ro.build.display.id)
if [ "$userdata_b_num" -eq 0 ] 
then
echo -e "- Not installed" > /data/data/com.david42069.dualboothelper/files/status.txt
echo "$BUILD_NUMBER" > /cache/dualboot/database/slota.txt
echo "$BUILD_NUMBER" > /data/data/com.david42069.dualboothelper/files/slota.txt
echo "Unavailable" > /cache/dualboot/database/slotb.txt
echo "Unavailable" > /data/data/com.david42069.dualboothelper/files/slotb.txt
else
if [ "$efs_b_num" -ne 0 ] 
then
echo -e "- Installed (V5)" > /data/data/com.david42069.dualboothelper/files/status.txt
else
echo -e "- Installed (V4)" > /data/data/com.david42069.dualboothelper/files/status.txt
fi
fi

#determine partition type
use_super=$(echo "$output" | $JQ_PATH -r '.disk.partitions[] | select(.name == "super") | .number')
if [ "$use_super" -ne 0 ] 
then
echo -e "- Device uses super partition" >> /data/data/com.david42069.dualboothelper/files/status.txt
else
if [ "$use_caps" -eq 1 ] 
then
echo -e "- Device uses normal naming partitions" >> /data/data/com.david42069.dualboothelper/files/status.txt
fi
if [ "$use_caps" -eq 2 ] 
then
echo -e "- Device uses caps naming partitions" >> /data/data/com.david42069.dualboothelper/files/status.txt
fi
fi
#End for device type start for storage type
if [ "$DISK" = "/dev/block/sda" ]
then
echo "- Device uses UFS (sda)" >> /data/data/com.david42069.dualboothelper/files/status.txt
fi
if [ "$DISK" = "/dev/block/sdc" ]
then
echo "- Device uses EMMC (sdc)" >> /data/data/com.david42069.dualboothelper/files/status.txt
fi
if [ "$DISK" = "/dev/block/mmcblk0" ]
then
echo "- Device uses EMMC (mmcblk0)" >> /data/data/com.david42069.dualboothelper/files/status.txt
fi

if [ "$userdata_a_num" -lt "$userdata_b_num" ]
then
echo "$BUILD_NUMBER" > /cache/dualboot/database/slota.txt
echo "$BUILD_NUMBER" > /data/data/com.david42069.dualboothelper/files/slota.txt
else
echo "$BUILD_NUMBER" > /cache/dualboot/database/slotb.txt
echo "$BUILD_NUMBER" > /data/data/com.david42069.dualboothelper/files/slotb.txt
fi
