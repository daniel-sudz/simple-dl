from __future__ import unicode_literals
#from java import *
import os
import youtubedl as youtube_dl

def download_youtube (url):
	#print(os.environ["HOME"])
	os.chdir(os.environ["HOME"])
	os.chdir('/sdcard/Download')

	import sys
	sys.stdout = open('logger.txt', 'w')

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