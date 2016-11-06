#
#   angara.ws:  meteo data
#   (client side) require 'ymap'
#

# global constant init section (must be first)

window.meteo =
  map_div_id:   "mapdiv"
  st_url:       "/meteo/old-ws/st"
  last_st_data: null
#

METEO = window.meteo
REFRESH_INTERVAL = 300*1000  # 5 minutes
# REFRESH_INTERVAL = 8000

_fetch_st_interval = null

fetch_st_data = ->
  $.getJSON(METEO.st_url).success (data) ->
    METEO.last_st_data = data
    METEO.update_st(data) if METEO.update_st?
#-

$ ->
  fetch_st_data()
  _fetch_st_interval = setInterval fetch_st_data, REFRESH_INTERVAL
#.
