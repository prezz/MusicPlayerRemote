As mentioned in the comments, starting with Ubuntu 18.04 and Linux Mint Tara you need to first sudo apt install qemu-kvm.

To check the ownership of /dev/kvm use

ls -al /dev/kvm
The user was root, the group kvm. To check which users are in the kvm group, use

grep kvm /etc/group
This returned

kvm\:x\:some_number:
on my system: as there is nothing rightwards of the final :, there are no users in the kvm group.

To add your user to the kvm group, you could use

sudo adduser $USER kvm
which adds the user to the group, and check once again with grep kvm /etc/group.

As mentioned by @marcolz, the command newgrp kvm should change the group membership live for you. If that did not work, @Knossos mentioned that you might want to log out and back in (or restart), for the permissions to take effect.
