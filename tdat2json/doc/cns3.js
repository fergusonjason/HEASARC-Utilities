Array.prototype.unique = function(b) {
    var a = [], i, l = this.length;
    for (i = 0; i < l; i++) {
        if (a.indexOf(this[i], 0, b) < 0) {
            a.push(this[i]);
        }
    }
    return a;
};

db.cns3.update({"_id":{$exists:true}}, {$set:{"xref":[]}}, false, true);

db.cns3.find().forEach(function(obj) {
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
    if (obj.lhsname != null) {
        obj.xref.push(obj.lhsname);
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
    db.cns3.save(obj);
});

db.cns3.update({"gliesename":{$exists:true}}, {$unset:{"gliesename":1}}, false, true);
db.cns3.update({"lhsname":{$exists:true}}, {$unset:{"lhsname":1}}, false, true);