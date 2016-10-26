import os, sys, subprocess, shutil

location = os.path.abspath('./');

def SEC():
	ps = subprocess.Popen('ps -ef | grep sec', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	if out.find("DHARMA/sensors/SEC/sec.rules") == -1:
		print 'Arrancando SEC'
		subprocess.Popen('sec --conf='+location+'/sensors/SEC/sec.rules --input='+location+'/sensors/SEC/asdf.log --input=/var/log/syslog --log='+location+'/sensors/events.log', shell=True)

def tsusen():
	ps = subprocess.Popen('ps -ef | grep tsusen', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	if out.find("python tsusen.py") == -1:
		shutil.copy(location+'/config_files/tsusen.conf', location+'/sensors/tsusen')
		os.chdir(location+"/sensors/tsusen")
		print 'Arrancando Tsusen'
		subprocess.Popen('python tsusen.py', shell=True)
	
def social():
	ps = subprocess.Popen('ps -ef | grep parsexcel.py', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	if -1 == -1:
		print 'Arrancando sensor social'
		os.chdir(location+"/sensors/social")
		subprocess.Popen('python parsexcel.py', shell=True)

def nodeJS():
	ps = subprocess.Popen('ps -ef | grep node', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	if out.find("node ./bin/www") == -1:
		os.chdir(location+'/BAGvisualization')
		print 'Arrancando NodeJS'
		subprocess.Popen('npm start', shell=True)

SEC()
tsusen()
social()
nodeJS()




