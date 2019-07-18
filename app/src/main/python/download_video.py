from __future__ import unicode_literals
#from java import *
import os
import youtubedl as youtube_dl

def download_youtube (url, path):
	#print(os.environ["HOME"])
	os.chdir(os.environ["HOME"])
	os.chdir('/storage/emulated/0/Download')

	import sys
	sys.stdout = open('logger.txt', 'w')
	sys.stderr = open('logger.txt', 'w')

	os.chdir(path)

	ydl_opts = {
		#'listformats':True
		#'cachedir': False,
		#'simulate': True,
		#'outtmpl': 'Internal shared storage/Download/%(title)s.%(ext)s'
	}
	with youtube_dl.YoutubeDL(ydl_opts) as ydl:
		try:
			ydl.download([str(url)])
		except:
			print('error')