import xlrd, time, syslog, os, sys

RISK_MAX = 50
last_value=""

file = sys.argv[1]

while True:
	workbook = xlrd.open_workbook('sensorSocial.xlsx')
	worksheet = workbook.sheet_by_index(1)
	risk =  worksheet.cell(10,16).value
	if risk != last_value:
		#syslog.syslog("HRScanner: el riesgo es "+str(risk))
		open(file, 'w').close()
		with open(file, "a") as myfile:
			myfile.write(str(risk)+'\r')
		last_value = risk	
	time.sleep(10)

