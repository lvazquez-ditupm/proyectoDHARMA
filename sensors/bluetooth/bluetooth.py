# -*- coding: utf-8 -*- 
# importamos los módulos para envio de correos y ejecución de comandos en la shell
from subprocess import Popen, PIPE
import subprocess, time, json, io, sys

open(sys.argv[2], "w").close()

while True:
    t_init = time.time()
    t_end = time.time()+int(sys.argv[1])
    data=""

    while time.time() < t_end:

        hcitoolResult = []
        #ubertoothResult = {}
        
        q = subprocess.Popen('hcitool scan', stdout=subprocess.PIPE, shell=True)
        
        hcitool = q.communicate()[0]

        '''
        p = Popen(['ubertooth-rx', '-t', '5'], stdout=PIPE)
        
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

        '''      
        
        hcitool = hcitool.splitlines()[1:]

        for line in hcitool:
            
            macaddr = line.split('\t')[1]
            
            if not macaddr in hcitoolResult:
                hcitoolResult.append(macaddr) 
        
        #data = data[:-1]+json.dumps({int(time.time()): {"LAPs" : ubertoothResult, "visibleAddr" : hcitoolResult}})[1:-1]+",}"
        data = json.dumps(hcitoolResult)[2:-2]
        

    print(data)
    
    if data != "":
        with io.FileIO(sys.argv[2], "a") as file:
            file.write(data+",")

