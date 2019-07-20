def download_latest_apk():
	import urllib.request, json, os
	with urllib.request.urlopen("https://api.github.com/repos/daniel-sudz/simple-dl/releases/latest") as url:
		data = json.loads(url.read().decode())
		currrelease = data["tag_name"]
		print(currrelease)
	url = "https://github.com/daniel-sudz/simple-dl/releases/download/"
	url += currrelease
	url += "/"
	url += currrelease
	url += ".apk"
	print(url)
	#wget.download(url, "/storage/emulated/0/Download")
	urllib.request.urlretrieve(url, "/storage/emulated/0/Download/" + currrelease + ".apk")

def return_name_latest_apk():
	import urllib.request, json, os
	with urllib.request.urlopen("https://api.github.com/repos/daniel-sudz/simple-dl/releases/latest") as url:
		data = json.loads(url.read().decode())
		currrelease = data["tag_name"]
		currrelease += ".apk"
		return currrelease
def return_latest_apk_size():
	import urllib.request, json, os
	with urllib.request.urlopen("https://api.github.com/repos/daniel-sudz/simple-dl/releases/latest") as url:
		data = json.loads(url.read().decode())
		apksize = data["assets"] # ["size"]
		apksize2 =apksize[0]
		apksize3 = apksize2["size"]
		return apksize3