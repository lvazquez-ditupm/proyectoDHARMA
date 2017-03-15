import os, sys, subprocess, shutil

location = os.path.abspath('./');

pathsFile = sys.argv[1]

open(pathsFile, 'w').close()

def SEC():
	ps = subprocess.Popen('ps -ef | grep sec', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	#with open(pathsFile, "a") as myfile:
	#	myfile.write("\r")
	if out.find("proyectoDHARMA/sensors/SEC/sec.rules") == -1:
		print 'Arrancando SEC'
		subprocess.Popen('sec --conf='+location+'/sensors/SEC/sec.rules --input='+location+'/sensors/SEC/asdf.log --input=/var/log/syslog --log='+location+'/sensors/SECevents.log', shell=True)

def tsusen():
	ps = subprocess.Popen('ps -ef | grep tsusen', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	with open(pathsFile, "a") as myfile:
		myfile.write("TSUSEN////var/log/tsusen\r")
	if out.find("python tsusen.py") == -1:
		shutil.copy(location+'/config_files/tsusen.conf', location+'/sensors/tsusen')
		os.chdir(location+"/sensors/tsusen")
		print 'Arrancando Tsusen'
		subprocess.Popen('python tsusen.py', shell=True)
	
def social():
	ps = subprocess.Popen('ps -ef | grep parsexcel.py', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	with open(pathsFile, "a") as myfile:
		myfile.write("SOCIAL///"+location+"/sensors/social\r")
	if out.find("python parsexcel.py") == -1:
		print 'Arrancando sensor social'
		os.chdir(location+"/sensors/social")
		subprocess.Popen('python parsexcel.py', shell=True)

def nodeJS():
	ps = subprocess.Popen('ps -ef | grep node', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	if out.find("node ./bin/www") == -1:
		os.chdir(location+'/GraphVisualization')
		print 'Arrancando NodeJS'
		subprocess.Popen('npm start', shell=True)

def ubertooth():
	ps = subprocess.Popen('ps -ef | grep ubertooth.py', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	with open(pathsFile, "a") as myfile:
		myfile.write("BLUETOOTH///"+location+"/sensors/bluetooth\r")
	if out.find("python ubertooth.py") == -1:
		print 'Arrancando sensor Bluetooth'
		os.chdir(location+"/sensors/bluetooth")
		subprocess.Popen('python ubertooth.py', shell=True)

def sensorUSB():
	ps = subprocess.Popen('ps -ef | grep SensorUSB.jar', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	with open(pathsFile, "a") as myfile:
		myfile.write("USB///"+location+'/sensors/sensorUSB\r')
	if out.find("java -jar SensorUSB.jar") == -1:
		os.chdir(location+'/sensors/sensorUSB')
		print 'Arrancando sensor USB'
		subprocess.Popen('java -jar SensorUSB.jar ./input.txt ./output.json root asdf 1000 138.4.7.191', shell=True)

def sensorPAE():
	ps = subprocess.Popen('ps -ef | grep SensorPAE.jar', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	with open(pathsFile, "a") as myfile:
		myfile.write("PAE///"+location+'/sensors/sensorPAE\r')
	if out.find("java -jar SensorPAE.jar") == -1:
		os.chdir(location+'/sensors/sensorPAE')
		print 'Arrancando sensor Presencia-Activos-Estado'
		subprocess.Popen('java -jar SensorPAE.jar exec 900 root asdf ./ ./', shell=True)


#SEC()
tsusen()
social()
#nodeJS()
ubertooth()
sensorUSB()
sensorPAE()



