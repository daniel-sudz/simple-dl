def run_custom_arguments (argument):
	import os, sys
	os.chdir('/storage/emulated/0/Download')

	sys.stdout = open('logger.txt', 'w')
	sys.stderr = open('logger.txt', 'w')
	
	sys.argv.append('https://www.youtube.com/watch?v=Ie4YyBbkUrw')
	sys.argv.append('-v')
	
	
	import youtubedl as youtube_dl 
	try:
		youtube_dl.main()
		sys.argv = [sys.argv[0]]
	except: 
		print("error has occured")