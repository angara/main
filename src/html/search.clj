
(ns html.search
  (:require
    [html.util :refer [inner-html]]
    [html.frame :refer [render-layout]]))
;


(defn yasearch [req]
  (render-layout req
    {:title "Яндекс Поиск"}

    (inner-html "

<div class='ya-site-form ya-site-form_inited_no'
  onclick=\"return {'action':'http://angara.net/yasearch/',
     'arrow':true,
     'bg':'#66B2FF',
     'fontsize':12,
     'fg':'#000000',
     'language':'ru',
     'logo':'rb',
     'publicname':'Поиск по Angara.Net',
     'suggest':true,
     'target':'_self',
     'tld':'ru',
     'type':3,
     'usebigdictionary':false,
     'searchid':1682884,
     'input_fg':'#000000',
     'input_bg':'#FFFFFF',
     'input_fontStyle':'normal',
     'input_fontWeight':'normal',
     'input_placeholder':null,
     'input_placeholderColor':'#000000',
     'input_borderColor':'#7F9DB9'
    }\">

<form action='https://yandex.ru/search/site/' method='get' target='_self'>
  <input type='hidden' name='searchid' value='1682884'/>
  <input type='hidden' name='l10n' value='ru'/>
  <input type='hidden' name='reqenc' value=''/>
  <input type='search' name='text' value=''/>
  <input type='submit' value='Найти'/></form>
</div>

<div id='ya-site-results'
  onclick=\"return {'tld':'ru',
    'language':'ru',
    'encoding':'',
    'htmlcss':'1.x',
    'updatehash':true
  }\"
></div>

<style type='text/css'>
  .ya-page_js_yes .ya-site-form_inited_no { display: none; }
</style>
")))




; <script type="text/javascript">
; (function(w,d,c){
;   var s=d.createElement('script'),
;       h=d.getElementsByTagName('script')[0],
;       e=d.documentElement;
;   if((' '+e.className+' ').indexOf(' ya-page_js_yes ')===-1){
;     e.className+=' ya-page_js_yes';
;   }
;   s.type='text/javascript';
;   s.async=true;
;   s.charset='utf-8';
;   s.src=(d.location.protocol==='https:'?'https:':'http:')+'//site.yandex.net/v2.0/js/all.js';
;   h.parentNode.insertBefore(s,h);
;   (w[c]||(w[c]=[])).push(function(){Ya.Site.Form.init()})
; })(window,document,'yandex_site_callbacks');
; </script>



;;.
