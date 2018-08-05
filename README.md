# mpdplay
MPD(Music Player Daemon)のスマホ用（Android）クライアントです。<br>
ラズベリーパイ３向けに作りました。<br>
Android Studio にインポートしてから、usb接続したスマホにインストールしてください。<br>
<br>
<a href="https://wiki.archlinux.jp/index.php/Music_Player_Daemon">MPD(Music Player Daemon)</a>

<h2>利用条件</h2><br>

ミュージックファイルは、mp3形式で、アーティスト、アルバム、タイトルが設定されていることが前提です。<br>

<h2>利用方法</h2>
・MPDをラズベリーパイ３にインストールし、ミュージックファイルをmusicフォルダにコピーします。<br>
・メニューのサーバー設定から、ラズベリーパイで稼働しているMPDのIPアドレスとポート番号を設定します。<br>
・プログラムを再起動すると曲名のリストが表示されます。<br>
・曲名のリストは、メニューから選択すると、アーティストごと、アルバムごとに<br>
　表示することができます。<br>
・曲名をチェックして、プレイボタンを押します。<br>
・プレイ中の曲名を曲名リストの上に表示します。<br>
・メニューのサーバー設定に、ラズベリーパイをシャットダウンするボタンがありますが<br>
　有効にするためには、サーバー側に、ラズベリーパイを　poweroffするホームページを<br>
　呼び出す仕組みが別途必要です。<br>
<h2>利用状況</h2>
SONYのh.ear goにUSB接続しています。特別なドライバーは不要でした。アウトプットをUSB側に<br>
切り替えるだけでOKです。<br>
<img src="mpdplay01.jpg"><br>
<br>
<h3>メニューのサーバー設定</h3>
サーバーをシャットダウンさせるボタンがあります。
<img src="mpdplay02.jpg"　width="256" height="480"><br>
