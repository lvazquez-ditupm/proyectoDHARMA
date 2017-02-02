# -*- coding: utf-8 -*- 
# importamos los módulos para envio de correos y ejecución de comandos en la shell
from subprocess import Popen, PIPE
import subprocess, time, json, io

while True:
    t_init = time.time()
    t_end = time.time()+(60*1)
    data=""

    while time.time() < t_end:

        hcitoolResult = []
        ubertoothResult = {}
        
        p = Popen(['ubertooth-rx', '-t', '5'], stdout=PIPE)
        q = subprocess.Popen('hcitool scan', stdout=subprocess.PIPE, shell=True)
        
        hcitool = q.communicate()[0]
        ubertooth = p.communicate()[0]
            
        ubertooth = ubertooth.splitlines()

        dictItem = {}

        for line in ubertooth:
            line = line.split(" ")
            
            dictItem = dict(map(str, x.split("=")) for x in line)

            if dictItem['snr'] <= 0 :
                continue;
            else:
                if dictItem['LAP'] in ubertoothResult:
                    ubertoothResult[dictItem['LAP']] = ubertoothResult[dictItem['LAP']]+1
                else:
                    ubertoothResult[dictItem['LAP']] = 1
                    
        dictItem = {}
        
        hcitool = hcitool.splitlines()[1:]
        
        for line in hcitool:
            
            macaddr = line.split('\t')[1]
            
            if not macaddr in hcitoolResult:
                hcitoolResult.append(macaddr) 
        
        data = data[:-1]+json.dumps({int(time.time()): {"LAPs" : ubertoothResult, "visibleAddr" : hcitoolResult}})[1:-1]+",}"
        #print data
    data = "{"+data[:-2]+"}"
    #print "FINAL"
    #print data

    with io.FileIO(str(int(t_init))+".json", "w") as file:
        file.write(data)
