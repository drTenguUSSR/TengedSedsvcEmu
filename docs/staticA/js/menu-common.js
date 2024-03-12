'use strict';

const menuTop=document.getElementById("menu-top")
const menuCmd=document.getElementById("menu-cmd")
const menuClassSelected="button_xmenu_selected"
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

function fileCfgInfo(fileLabel, fileData) {
    let result=`fileUse: ${fileLabel}:`+" info: "
    + fileData.info
    + (('multiple' in fileData && fileData.multiple)?",files-multi":",file-one")
    + ", extensions="+fileData.extensions
    return result
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

//======================== mainFunc section
function testYamlLoad(event) {
    //topMenuInfo()
    console.log("testYamlLoad-beg")
    //const resultX=testYamlLoadSubA()
    const resultX=testYamlLoad_2()
    console.log("testYamlLoad-end")
}

async function testYamlLoad_2() {
    const response=await fetch("api/data-main.yaml")
    const dataText=await response.text()
    const ydoc = jsyaml.load(dataText);
    const baseRel=ydoc.common['base-rel']
    console.log("baseRel=["+baseRel+"]")

    //selTab - выбранная вкладка из topMenu, содержит массив
    const selTab = ydoc['tab-standart']
    console.log("prop-beg")
    
    let tLabel=""
    let tLen=0
    //selSvc - описание конкретного сервиса
    for(const selSvc of selTab) {
        console.log("---")
        console.log("svc.id="+selSvc.id)
        console.log("svc.rel=!"+selSvc.rel+"!")
        console.log("request["+selSvc.request+"]")
        console.log("last=!"+selSvc['some-other']+"!")
        const xLabel=selSvc.label
        if(xLabel.length > tLen) {
            tLabel=xLabel
            tLen=tLabel.length
        }
        if ('fileA' in selSvc) {
            console.log(fileCfgInfo('fileA',selSvc.fileA))
        }
        if ('fileB' in selSvc) {
            console.log(fileCfgInfo('fileB',selSvc.fileB))
        }
    }

    console.log("prop-end")
    console.log(`max: ${tLabel}, len=${tLen}`)

    return "xx1"
}

async function testYamlLoad_1() {
    const response=await fetch("api/data-a.yaml")
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

//========================
function testQueryTopElements(event) {
    console.log("testQueryTopElements-beg")
    const filter = '[id^="tab-"]'
    console.log(`filter:!${filter}!`)
    const topAllBtn=menuTop.querySelectorAll(filter)
    console.log(topAllBtn)
    for(var elButton of topAllBtn) {
        let bName=elButton.innerText
        console.log(`id:${elButton.id} bName:${bName}`)
    }
    console.log("testQueryTopElements-end")
}

//========================
const filterTopMenuButtons='[id^="tab-"]'
const button_hide="button_hide"

function testLoadYamlAndHideTop(event) {
    console.log("testLoadYamlAndHideTop-beg")
    const topAllBtnEl=menuTop.querySelectorAll(filterTopMenuButtons)
    const topAllBtnId = new Set()
    for(const elButtonEl of topAllBtnEl) {
        topAllBtnId.add(elButtonEl.id)
    }
    console.log("topTab:"+Array.from(topAllBtnId).join(","))
    testLoadYamlAndHideTop_s(topAllBtnEl,topAllBtnId)
    console.log("testLoadYamlAndHideTop-end")
}

async function testLoadYamlAndHideTop_s(topAllBtnEl,topAllBtnId) {
    const response=await fetch("api/data-main.yaml")
    const dataText=await response.text()
    let ydoc = jsyaml.load(dataText);
    const baseRel=ydoc.common['base-rel']
    console.log("baseRel=["+baseRel+"]")

    const hideSect = new Set(topAllBtnId)
    for(const sect in ydoc) {
        console.log(`file-sect: ${sect}`)
        hideSect.delete(sect)
    }
    console.log("hideSect list:"+Array.from(hideSect).join(","))
    const childs=menuTop.childNodes
    for(const elButtonEl of topAllBtnEl) {
        //topAllBtnId.add(elButtonEl.id)
        console.log(`hiding id=${elButtonEl.id} check=${hideSect.has(elButtonEl.id)}`)
        if (hideSect.has(elButtonEl.id)) {
            elButtonEl.classList.add(button_hide)
        }
    }
    console.log("_s end")
    return "xx1"
}
//======================== selFiles section
function testFilesInfo(event) {
    console.log("clickFiles-BEG")
    console.log(fileAinputFile.files)
    console.log(fileBinputFile.files)
    console.log(fileCinputFile.files)
    console.log("clickFiles-END")
}