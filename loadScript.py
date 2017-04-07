import os, sys, subprocess, shutil

location = os.path.abspath('./')

pathsFile = sys.argv[1]

open(pathsFile, "w").close()

def SEC():
	ps = subprocess.Popen('ps -ef | grep sec', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	if out.find("sensors/SEC/sec.rules") == -1:
		print 'Arrancando SEC'
		subprocess.Popen('sec --conf='+location+'/sensors/SEC/sec.rules --input='+location+'/sensors/SEC/SECinput.log --log='+location+'/sensors/SEC/SECevents.log', shell=True)

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
		myfile.write("SOCIAL///"+location+"/sensors/social/output.txt\r")
	if out.find("python parsexcel.py") == -1:
		print 'Arrancando sensor social'
		os.chdir(location+"/sensors/social")
		subprocess.Popen('python parsexcel.py ./output.txt', shell=True)

def nodeJS():
	ps = subprocess.Popen('ps -ef | grep node', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	if out.find("node ./bin/www") == -1:
		os.chdir(location+'/GraphVisualization')
		print 'Arrancando NodeJS'
		subprocess.Popen('npm start', shell=True)

def bt1():
	ps = subprocess.Popen('ps -ef | grep bluetooth.py', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	if out.find("python bluetooth.py") == -1:
		print 'Arrancando sensor Bluetooth'
		os.chdir(location+"/sensors/bluetooth")
		subprocess.Popen('python bluetooth.py 60 ./input.txt', shell=True)

def bt2():
	ps = subprocess.Popen('ps -ef | grep SensorBluetooth.jar', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	if out.find("java -jar SensorBluetooth.jar") == -1:
		print 'Arrancando procesador Bluetooth'
		os.chdir(location+"/sensors/bluetooth")
		subprocess.Popen('java -jar SensorBluetooth.jar exec 60 root asdf ./input.txt ./output.txt', shell=True)

def bluetooth():
	with open(pathsFile, "a") as myfile:
		myfile.write("BLUETOOTH///"+location+"/sensors/bluetooth/output.txt\r")
	#bt1()
	bt2()

'''
def corr1():
	ps = subprocess.Popen('ps -ef | grep Rscript', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	if out.find("java -jar SensorBluetooth.jar") == -1:
		print 'Arrancando correlador (motor de correlacion)'
		os.chdir(location+"/sensors/bluetooth")
		subprocess.Popen('java -jar SensorBluetooth.jar exec 60 root asdf ./input.txt ./output.txt', shell=True)

'''
def corr2():
	with open(pathsFile, "a") as myfile:
		myfile.write("CORRELATOR///"+location+'/../becadit/storedData\r')

def correlator():
	#corr1()
	corr2()


def sensorUSB():
	ps = subprocess.Popen('ps -ef | grep SensorUSB.jar', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	with open(pathsFile, "a") as myfile:
		myfile.write("USB///"+location+'/sensors/sensorUSB/output.txt\r')
	if out.find("java -jar SensorUSB.jar") == -1:
		os.chdir(location+'/sensors/sensorUSB')
		print 'Arrancando sensor USB'
		subprocess.Popen('java -jar SensorUSB.jar ./input.txt ./output.txt root asdf 5000 138.4.7.191', shell=True)

def sensorPAE():
	ps = subprocess.Popen('ps -ef | grep SensorPAE.jar', stdout=subprocess.PIPE, shell=True)
	(out, err) = ps.communicate()
	with open(pathsFile, "a") as myfile:
		myfile.write("PAE///"+location+'/sensors/sensorPAE/output.txt\r')
	if out.find("java -jar SensorPAE.jar") == -1:
		os.chdir(location+'/sensors/sensorPAE')
		print 'Arrancando sensor Presencia-Activos-Estado'
		subprocess.Popen('java -jar SensorPAE.jar exec 60 root asdf ./input.txt ./output.txt', shell=True)


#SEC()
#nodeJS()
#tsusen()
#social()
#sensorUSB()
#bluetooth()
#sensorPAE()
correlator()


