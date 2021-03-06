// Generated by CoffeeScript 1.8.0
(function() {
  var ERRMSG_TEXT, get_cookie, post_errmsg, str, x, _d02,
    __indexOf = [].indexOf || function(item) { for (var i = 0, l = this.length; i < l; i++) { if (i in this && this[i] === item) return i; } return -1; };

  window.lib = x = {};

  if (!String.prototype.trim) {
    String.prototype.trim = function() {
      return this.replace(/^\s+|\s+$/g, "");
    };
  }

  if (!Array.prototype.forEach) {
    Array.prototype.forEach = function(action, that) {
      var i, n;
      i = 0;
      n = this.length;
      while (i < n) {
        if (__indexOf.call(this, i) >= 0) {
          action.call(that, this[i], i, this);
        }
        i++;
      }
      return null;
    };
  }

  x.int = function(s, def) {
    var i;
    i = parseInt(s, 10);
    if (!isNaN(i)) {
      return i;
    } else {
      return def;
    }
  };

  x.str = str = function(s) {
    if ((s == null) || (typeof s === "number" && isNaN(s))) {
      return "";
    } else {
      return "" + s;
    }
  };

  x.randInt = function(n) {
    return Math.floor(Math.random() * n);
  };

  x.htmlq = function(s) {
    return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  };

  ERRMSG_TEXT = {
    parsererror: "Ошибка в ответе сервера.",
    timeout: "Таймаут запроса.",
    error: "Ошибка при соединении с сервером.",
    abort: "Запрос прерван.",
    nimp: "Функциональность не реализована.",
    db: "Ошибка базы данных.",
    sys: "Ошибка на сервере.",
    req: "Ошибка в запросе."
  };

  x.errmsg = function(err) {
    if (err != null ? err.msg : void 0) {
      return err.msg;
    }
    return ERRMSG_TEXT[err != null ? err.err : void 0] || "Неизвестная ошибка!";
  };

  post_errmsg = function(err) {
    return alert(x.errmsg(err));
  };

  x.get_cookie = get_cookie = function(name) {
    var c, cookies, nl, _i, _len, _ref;
    cookies = (_ref = window.document.cookie) != null ? _ref.split(';') : void 0;
    if (!cookies) {
      return;
    }
    name += "=";
    nl = name.length;
    for (_i = 0, _len = cookies.length; _i < _len; _i++) {
      c = cookies[_i];
      c = c.trim();
      if (c.substring(0, nl) === name) {
        return decodeURIComponent(c.substring(nl));
      }
    }
  };

  x.csrf_token = function() {
    return window._csrf != null ? window._csrf : window._csrf = get_cookie("_csrf") || false;
  };

  x.AJAX_TIMEOUT = 100 * 1000;

  x.post = function(url, data, success, error) {
    if (error == null) {
      error = post_errmsg;
    }
    return $.ajax({
      url: url,
      data: JSON.stringify(data),
      timeout: x.AJAX_TIMEOUT,
      type: 'POST',
      dataType: 'json',
      contentType: 'application/json',
      beforeSend: function(xhr) {
        var csrf;
        if ((csrf = x.csrf_token())) {
          return xhr.setRequestHeader('x-csrf-token', csrf);
        }
      },
      success: function(data) {
        if (data != null ? data.ok : void 0) {
          return success(data);
        } else {
          return error(data);
        }
      },
      error: function(xhr, status, text) {
        return error({
          err: status,
          text: text
        });
      }
    });
  };

  x.get = function(url, data, success, error) {
    if (error == null) {
      error = post_errmsg;
    }
    return $.ajax({
      url: url,
      data: data,
      type: 'GET',
      dataType: 'json',
      timeout: x.AJAX_TIMEOUT,
      success: function(data) {
        if (data != null ? data.ok : void 0) {
          return success(data);
        } else {
          return error(data);
        }
      },
      error: function(xhr, status, text) {
        return error({
          err: status,
          text: text
        });
      }
    });
  };

  x.trace = function(qs, data) {
    return $.ajax({
      url: '/_trace' + (qs && ("?" + qs) || ""),
      type: 'POST',
      timeout: 30 * 1000,
      dataType: 'json',
      contentType: 'application/json',
      data: JSON.stringify(data),
      done: function() {}
    });
  };

  _d02 = function(d) {
    if (d < 10) {
      return "0" + d;
    } else {
      return "" + d;
    }
  };

  x.hhmm = function(date) {
    if (!date) {
      return "??:??";
    }
    return _d02(date.getHours()) + ":" + _d02(date.getMinutes());
  };

  x.hhmmss = function(date) {
    if (!date) {
      return "??:??:??";
    }
    return _d02(date.getHours()) + ":" + _d02(date.getMinutes()) + ":" + _d02(date.getSeconds());
  };

  x.ddmmyyyy = function(date) {
    if (!date) {
      return "??.??.????";
    }
    return _d02(date.getDate()) + "." + _d02(date.getMonth() + 1) + "." + date.getFullYear();
  };

  x.cleanup_phone = function(phone) {
    phone = str(phone).replace(/[^0-9\+]/gi, '');
    if (phone[0] === '8') {
      phone = phone.substr(1);
    }
    if (phone.length === 10) {
      phone = "7" + phone;
    }
    if (phone[0] !== '+') {
      phone = "+" + phone;
    }
    if (phone.match(/^\+7\d{10}$/)) {
      return phone;
    } else {
      return "";
    }
  };

  x.cleanup_email = function(eml) {
    eml = str(eml).trim();
    if (eml.length > 80) {
      return "";
    }
    if (!eml.match(/^[0-9a-z\.\-\_]+@([0-9a-z\-]+\.)+([a-z]){2,}$/i)) {
      return "";
    }
    return eml;
  };

  x.delayed_handler = function(func, args, timeout) {
    var th;
    if (timeout == null) {
      timeout = 200;
    }
    th = null;
    return function() {
      if (th) {
        return;
      }
      return th = setTimeout(function() {
        func(args);
        return th = null;
      }, timeout);
    };
  };

  x.ON_CHANGE_EVENTS = "blur change keyup keypress cut paste";

}).call(this);
