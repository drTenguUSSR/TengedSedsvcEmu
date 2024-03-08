'use strict';

const menuTop=document.getElementById("menu-top")
const menuCmd=document.getElementById("menu-cmd")
const menuClassSelected="button-menu__item_selected"
const requestType=document.getElementById("request-type")
const requestText=document.getElementById("request-text")
const fileAinputFile=document.getElementById("fileAinput")
fileAinputFile.info="fileAinputInfo"
const fileBinputFile=document.getElementById("fileBinput")
fileBinputFile.info="fileBinputInfo"
const fileCinputFile=document.getElementById("fileCinput")
fileCinputFile.info="fileCinputInfo"

function domReady() {
    console.log('DOMready');
    fileAinputFile.addEventListener("change", fileChanges);
    fileBinputFile.addEventListener("change", fileChanges);
    fileCinputFile.addEventListener("change", fileChanges);
}


document.addEventListener("DOMContentLoaded", domReady);

// console.log("document.readyState="+document.readyState); 
//  => document.readyState=interactive


function fileChanges(fileInput) {
    console.log("fileChanges-beg")
    let info=""
    console.log(fileInput)
    let elInfo=document.getElementById(fileInput.target.info)
    console.log(elInfo)
    for (const file of fileInput.target.files) {
        console.log("name="+file.name+" size="+file.size+" type="+file.type)
        info+=file.name+","+file.size+" bytes|"
    }
    info=info.slice(0,-1)
    elInfo.innerText=info
    console.log("fileChanges-end")
}

function buttonsRefresh(event, menuRoot) {
    const selectedId=event.target.id;
    console.log("click on ["+selectedId+"]");
    for(const sel of menuRoot.children) {
        if (sel.id==selectedId) {
            if (!sel.classList.contains(menuClassSelected)) {
                sel.classList.add(menuClassSelected);
            }
            sel.disabled=true
        } else {
            if (sel.classList.contains(menuClassSelected)) {
                sel.classList.remove(menuClassSelected);
            }
            sel.disabled=false
        }
    }
}

function clickTopMenu(event) {
    buttonsRefresh(event,menuTop)
}

function clickCmdMenu(event) {
    //read: requestText.value
    buttonsRefresh(event,menuCmd)
    const id=event.target.id;
    const text=`{
some text ${id} 3
    }`;
}

function testYamlLoad(event) {
    console.log("testYamlLoad-beg")
    const resultX=testYamlLoadSub()
    console.log(resultX)
    console.log("testYamlLoad-end")
}

async function testYamlLoadSub() {
    const response=await fetch("api/test-data-1.yaml")
    console.log("status="+response.status)
    const dataText=await response.text()
    let ydoc = jsyaml.load(dataText);
    console.log(ydoc)
    console.log("text=["+ydoc.dat1.dat2.fieldA+"]")
    console.log(typeof ydoc.dat2)
    console.log("dat5:")
    console.log(ydoc.dat5)
    console.log(typeof ydoc.dat5)
    console.log("!["+ydoc.dat4+"]!")

    console.log("prop-beg")
    for(const prop in ydoc.port_mapping) {
        console.log(`prop !${prop}! is !${ydoc.port_mapping[prop]}!`)
    }
    console.log("prop-end")

    return "xx1"
}

function clickFiles(event) {
    console.log("clickFiles-BEG")
    console.log(fileAinputFile.files)
    console.log(fileBinputFile.files)
    console.log(fileCinputFile.files)
    console.log("clickFiles-END")
}

function changeFormAction(elName) {
    console.log("form-action-beg")
    console.log("elName=["+elName+"]")
    let elForm=document.getElementById(elName)
    console.log(elForm.method)
    console.log(elForm.action)
    elForm.action="http://localhost:8070/lost-space"
    console.log(elForm.action)
    console.log("form-action-end")
}

function exampleRequestReset(event) {
    console.log("exampleRequestReset-beg")
    exampleRequestResetSub("fileAinput")
    exampleRequestResetSub("fileBinput")
    exampleRequestResetSub("fileCinput")
    console.log("exampleRequestReset-end")
}

function exampleRequestResetSub(label) {
    let elInf=document.getElementById(label+"Info")
    elInf.innerText="";
}