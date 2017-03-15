import xlrd, time, syslog, os

RISK_MAX = 50
last_value=""
while True:
	workbook = xlrd.open_workbook('sensorSocial.xlsx')
	worksheet = workbook.sheet_by_index(1)
	risk =  worksheet.cell(10,16).value
	if risk != last_value:
		#syslog.syslog("HRScanner: el riesgo es "+str(risk))
		with open("./anomaly.txt", "a") as myfile:
			myfile.write(str(risk)+'\r')
		last_value = risk	
	time.sleep(10)

