// import ONLY hd first... if we have to reprocess too much, we'll end up having to import into separate collections
// then combine them later

db.catalog.find({"hdname":{$exists:true}}).forEach(function(obj1) {
    var hdname = obj1.name;
    if (hdname != null) {
        var prefix = "HD ";
        var identifier = hdname.substring(2);
        obj1.name = prefix + identifier;
        db.catalog.save(obj1);
    }
});

// at this point, import the remaining json files

// add the xref collection to everything
db.catalog.update({"_id":{$exists:true}}, {$set:{"xref":[]}}, false, true);


db.catalog.find().forEach(function(obj) {
    // star catalogs
    if (obj.bscname != null) {
        obj.xref.push(obj.bscname);
    }
    if (obj.cpdname != null) {
        obj.xref.push(obj.cpdname);
    }
    if (obj.dmname != null) {
        obj.xref.push(obj.dmname);
    }
    if (obj.gcname != null) {
        obj.xref.push(obj.gcname);
    }
    if (obj.gliesename != null) {
        obj.xref.push(obj.gliesename);
    }
    if (obj.hdname != null) {
        obj.xref.push(obj.hdname);
    }
    if (obj.hipname != null) {
        obj.xref.push(obj.hipname);
    }
    if (obj.ppmname != null) {
        obj.xref.push(obj.ppmname);
    }
    if (obj.saoname != null) {
        obj.xref.push(obj.saoname);
    }

    // galaxy catalogs
    if (obj.messiername != null) {
        obj.xref.push(obj.messiername);
    }
    if (obj.ngc2000name != null) {
        obj.xref.push(obj.ngc2000name);
    }
    if (obj.ugcname != null) {
        obj.xref.push(obj.ugcname);
    }
    db.catalog.save(obj);
});

// unset the fields now that they are in the array
db.catalog.update({"bscname":{$exists:true}}, {$unset:{"bscname":1}}, false, true);
db.catalog.update({"hdname":{$exists:true}}, {$unset:{"hdname":1}}, false, true);
db.catalog.update({"saoname":{$exists:true}}, {$unset:{"saoname":1}}, false, true);
db.catalog.update({"ppmname":{$exists:true}}, {$unset:{"ppmname":1}}, false, true);
db.catalog.update({"hipname":{$exists:true}}, {$unset:{"hipname":1}}, false, true);
db.catalog.update({"gcname":{$exists:true}}, {$unset:{"gcname":1}}, false, true);
db.catalog.update({"dmname":{$exists:true}}, {$unset:{"dmname":1}}, false, true);
db.catalog.update({"gliesename":{$exists:true}}, {$unset:{"gliesename":1}}, false, true);
db.catalog.update({"cpdname":{$exists:true}}, {$unset:{"cpdname":1}}, false, true);
db.catalog.update({"messiername":{$exists:true}}, {$unset:{"messiername":1}}, false, true);
db.catalog.update({"ngc2000name":{$exists:true}}, {$unset:{"ngc2000name":1}}, false, true);
db.catalog.update({"ugcname":{$exists:true}}, {$unset:{"ugcname":1}}, false, true);

db.catalog.find({"alt_name":{$exists:true}}).forEach(function(obj) {
    var altname = obj.alt_name;
    if (altname.indexOf("NGC") != -1) {
        obj.xref.push(altname);
        db.catalog.save(obj);
    }
    if (altname.indexOf("M ") != -1) {
        obj.xref.push(altname);
        db.catalog.save(obj);
    }
});

db.catalog.update({alt_name:/NGC/i}, {$unset:{"alt_name":1}}, false, true);
db.catalog.update({alt_name:/^M\s[0-9]+/i}, {$unset:{"alt_name":1}}, false, true);
db.catalog.update({alt_name:/^NOVA/i}, {$unset:{"alt_name":1}}, false, true);

// still need to process the bayer and flamsteed designations from the alt_name
db.catalog.find({alt_name:/[A-Za-z]{3}\s[A-Za-z]{3}/i}).forEach(function(obj) {
    var name = obj.alt_name;
    obj.xref.push(name);
    db.catalog.save(obj);
});
db.catalog.update({alt_name:/[A-Za-z]{3}\s[A-Za-z]{3}/i}, {$unset:{alt_name: 1}}, false, true);

db.catalog.find({alt_name:/[A-Za-z]{2}\s\s[A-Za-z]{3}/i}).forEach(function(obj) {
    var name = obj.alt_name;
    obj.xref.push(name);
    db.catalog.save(obj);
});
db.catalog.update({alt_name:/[A-Za-z]{2}\s\s[A-Za-z]{3}/i}, {$unset:{alt_name: 1}}, false, true);

db.catalog.find({alt_name:/[A-Z][a-z]+\s*[0-9]+[A-Za-z]{3}/i}).forEach(function(obj) {
    var name = obj.alt_name;
    var pattern = /^([A-Z][a-z]+)\s*([0-9]+)([A-Za-z]{3})/i;
    var matchArray = name.match(pattern);
    if ((matchArray != null) && (matchArray.length = 4)) {
        var name1 = matchArray[1] + " " + matchArray[3];
        var name2 = matchArray[2] + " " + matchArray[3];
        obj.xref.push(name1);
        obj.xref.push(name2);
    }
    db.catalog.save(obj);
});

// I probably could have tweaked the previous function to do this in one pass, but didn't realize it wasn't going
// to catch everything
db.catalog.update({alt_name:/[A-Z][a-z]+\s*[0-9]+[A-Za-z]{3}/i}, {$unset:{alt_name: 1}}, false, true);

db.catalog.find({alt_name:/^[0-9]+\s*[A-Za-z]{3}/i}).forEach(function(obj) {
    var name = obj.alt_name;
    var pattern = /^([0-9]+)\s*([A-Za-z]{3})/i;
    var matchArray = name.match(pattern);
    if ((matchArray != null) && (matchArray.length == 3)) {
        var name = matchArray[1] + " " + matchArray[2];
        obj.xref.push(name);
    }
    db.catalog.save(obj);
});
db.catalog.update({alt_name:/^[0-9]+\s*[A-Za-z]{3}/i}, {$unset:{alt_name:1}}, false, true);
db.catalog.update({alt_name:{$exists:true}}, {$unset:{alt_name:1}});
db.catalog.update({other_name:/^W$/i}, {$unset:{other_name:1}}, false, true);

function updateArrays() {
db.catalog.find().forEach(function(obj) {
    var objXref = obj.xref;
    if (objXref.length > 1) {
        for (var name in objXref) {
            var doc = db.catalog.find({"name":name});
            db.catalog.update({"name":name},{$addToSet:{$each:objXref}});
        }
    }
});
}

// do not use items below this.
// remove the entire xref collection
db.catalog.update({"xref":{$exists:true}}, {$unset:{"xref":1}}, false, true);

// add the xref collection to everything
db.catalog.update({"_id":{$exists:true}}, {$set:{"xref":[]}}, false, true);
db.catalog.find({alt_name:{$exists:true}});
db.catalog.find({alt_name:/[A-Z][a-z]+\s+[0-9]+[A-Za-z]{3}/i});