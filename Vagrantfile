# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|

  config.vm.box = "bento/ubuntu-14.04"

  config.vm.network "forwarded_port", guest: 80, host: 8080 # NGINX
  config.vm.network "forwarded_port", guest: 20815, host: 20815 # RethinkDB

  config.vm.provision "shell", inline: <<-SHELL
    if [ ! -f /opt/ansible.installed ]; then
      apt-get update
      apt-get install -y software-properties-common python-software-properties
      add-apt-repository -y ppa:ansible/ansible-1.9
      apt-get update
      apt-get install -y ansible

      touch /opt/ansible.installed
    fi
  SHELL

  config.vm.provision "shell", inline: <<-SHELL
    if [ ! -f /opt/ansible-galaxy-roles.installed ]; then
      ansible-galaxy install jdauphant.nginx

      touch /opt/ansible-galaxy-roles.installed
    fi
  SHELL

  config.vm.provision "ansible_local" do |ansible|
    ansible.install = false
    ansible.playbook = "scripts/playbook.yml"
    ansible.verbose = "vvvv"
  end
end
