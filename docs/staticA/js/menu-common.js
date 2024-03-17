'use strict';

let entryPointUrl = "" //установленный URL для точки входа
let entryPointData = "" //загруженный и распарсенный JSON из точки входа
let сonfigYamlData = "" // загруженный и распарсенный YAML настройки
let selectedTopSection = "" // текущая выбранная в top меню секция

const сonfigYamlPath = "api/data-main.yaml"
const filterTopMenuButtons='[id^="tab-"]'
const filterCmdMenuButtons='[id^="cmd-"]'
const block_hide="block_hide"

const menuTop = document.getElementById("menu-top")
const menuCmd = document.getElementById("menu-cmd")
const menuClassSelected="button_xmenu_selected"
const requestType=document.getElementById("request-type")
const requestText=document.getElementById("request-text")
const fileAinputFile=document.getElementById("fileAinput")
fileAinputFile.info="fileAinputInfo"
const fileBinputFile=document.getElementById("fileBinput")
fileBinputFile.info="fileBinputInfo"
const fileCinputFile=document.getElementById("fileCinput")
fileCinputFile.info="fileCinputInfo"

const elRpanelEntryPoint = document.getElementById("r-panel__entry-point")
const elRpanelEntryText = document.getElementById("r-panel__entry-text")
const elRpanelEntryHref = document.getElementById("r-panel__entry-href")



const elRPanelActionField= document.getElementById("form-action")
const elRPanelActionForm= document.getElementById("example-request")

const elRpanelRequestText = document.getElementById("request-text")
const elRpanelContentType = document.getElementById("request-type")
const elRpanelRelation = document.getElementById("request-relation")

function domReady() {
    console.log('DOMready')
    fileAinputFile.addEventListener("change", fileChanges)
    fileBinputFile.addEventListener("change", fileChanges)
    fileCinputFile.addEventListener("change", fileChanges)
    simOnload()
}

document.addEventListener("DOMContentLoaded", domReady);

async function dialogEntryPoint(event) {
    let currEntry = entryPointUrl || "http://localhost:8070/ext-sedsvc/entry-point/"
    let res =  window.prompt("введите адрес entry point СЭДсервиса\n'~' для api/example-root.json", currEntry);
    if (res == null ) {
        console.log("dialog canceled")
        return
    }
    entryPointUrl = res
    console.log(`entryPointUrl=${entryPointUrl}`)
    let elEnt = document.getElementById("q-entry")
    elEnt.innerHTML=`${entryPointUrl}`

    if (entryPointUrl.length<3 && entryPointUrl != '~') {
        alert("адрес точки доступа не указан")
        return
    }
    let response
    try {
        if (entryPointUrl == "~") {
            console.log("fetch: overload to build in [api/example-root.json]")
            response = await fetch("api/example-root2.json")
        } else {
            console.log("fetch: loading")
            response = await fetch(entryPointUrl)
        }
        const cfgData = await response.json()
        console.log(`loaded cfg.title=${cfgData.title}`)
        entryPointData = cfgData
        elRpanelEntryText.innerText = ""
        elRpanelEntryPoint.classList.remove("block_init")
        elRpanelEntryPoint.classList.add("block_pass")
    } catch (ex) {
        console.log("error!")
        console.log(ex)
        elRpanelEntryText.innerText = ex
        elRpanelEntryPoint.classList.remove("block_init")
        elRpanelEntryPoint.classList.add("block_error")
    }
    console.log("checkEntryPoint-END")
}


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

//====================== simOnLoad common functions
function buttonsRefresh(selectedId, menuRoot) {
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
//====================== simOnload

async function simOnload() {
    console.log("simOnload-beg")
    console.log(`cfg ${сonfigYamlPath}. load and parse`)
    const response=await fetch(сonfigYamlPath)
    const dataText=await response.text()
    const ydoc = jsyaml.load(dataText)
    const baseRel=ydoc.common['base-rel']
    console.log(`done. baseRel=[${baseRel}]`)
    processTopMenu(ydoc)
    clearAside()
    сonfigYamlData=ydoc
    console.log("simOnload-end")
}

function processTopMenu(ydoc) {
    const topAllBtnEl=menuTop.querySelectorAll(filterTopMenuButtons)
    const topAllBtnId = new Set()

    for(const elButtonEl of topAllBtnEl) {
        topAllBtnId.add(elButtonEl.id)
    }
    console.log("topTab-all:"+Array.from(topAllBtnId).join(","))

    const hideSect = new Set(topAllBtnId)
    for(const sect in ydoc) {
        console.log(`cfg: found section: ${sect}`)
        hideSect.delete(sect)
    }
    console.log("hideSect list:"+Array.from(hideSect).join(","))
    const childs=menuTop.childNodes
    for(const elButtonEl of topAllBtnEl) {
        let toHide = false
        if (hideSect.has(elButtonEl.id)) {
            elButtonEl.classList.add(block_hide)
            toHide=true
        }
        console.log(` id=${elButtonEl.id} check=${hideSect.has(elButtonEl.id)} toHide=${toHide}`)
    }
    console.log("processTopMenu done")
}

function clearAside() {
    const cmdAllBtnEl=menuCmd.querySelectorAll(filterCmdMenuButtons)
    console.log("clearAside: toClear"+Array.from(cmdAllBtnEl).join(","))
    for(const elCmd of cmdAllBtnEl) {
        elCmd.remove()
    }
    console.log("clearAside done")
}
//================= sim: process topMenuClick
function clickTopMenu(event) {
    const selectedId=event.target.id
    selectedTopSection = selectedId
    buttonsRefresh(selectedId,menuTop)
    console.log("eventId="+selectedId)
    clearAside()

    const cfgAsideList=сonfigYamlData[selectedId]

    for( const cfgCmd of cfgAsideList) {
        console.log(`- id:${cfgCmd.id}`)
        const cmdButton = document.createElement('button')
        cmdButton.id = cfgCmd.id
        cmdButton.innerText = cfgCmd.label
        cmdButton.classList.add("smenu__item")
        cmdButton.classList.add("button_xmenu")
        cmdButton.onclick = clickCmdMenu
        menuCmd.appendChild(cmdButton)
    }
    console.log("clickTopMenu done")
}

//================= sim: clickCmdMenu
function clickCmdMenu(event) {
    const selectedId=event.target.id
    console.log(`clickCmdMenu: ${selectedId} in sect: ${selectedTopSection}`)
    buttonsRefresh(selectedId,menuCmd)
    const cfgSection = сonfigYamlData[selectedTopSection]
    const cfgCmd = cfgSection.find(obj => obj.id === selectedId)
    console.log("found-cmd:")
    console.log(cfgCmd)

    clickCmdMenuFile("fileA",cfgCmd)
    clickCmdMenuFile("fileB",cfgCmd)
    clickCmdMenuFile("fileC",cfgCmd)

    elRpanelRequestText.value = cfgCmd.request
    elRpanelContentType.value = cfgCmd['content-type']
    const fullRel = сonfigYamlData.common['base-rel']+cfgCmd.rel
    console.log(`fullRel=${fullRel}`)
    elRpanelRelation.innerText = fullRel
    console.log(typeof entryPointData.entry)
    const formAction = entryPointData.entry.find(obj => obj.rel == fullRel)?.href

    if (formAction !=null) {
        console.log(`formAction=${formAction}`)
        elRPanelActionField.innerText = formAction
        elRPanelActionForm.action = formAction
        elRpanelEntryHref.classList.remove("block_error")
        elRpanelEntryText.innerText = ""
    } else {
        const msg = "в точке входа отсутствует relation"
        elRpanelEntryHref.classList.add("block_error")
        elRpanelEntryText.innerText = msg
    }

    console.log("clickCmdMenu done")
}

function clickCmdMenuFile(prefix,cfgCmd) {
    const suffixBlock="block"
    const suffixLabel="inputLabel"
    const suffixInfo="inputInfo"
    const suffixFile="input"

    const elBlock = document.getElementById(prefix+suffixBlock)
    const elLabel = document.getElementById(prefix+suffixLabel)
    const elInfo = document.getElementById(prefix+suffixInfo)
    const elFile = document.getElementById(prefix+suffixFile)
    if (cfgCmd.hasOwnProperty(prefix)) {
        console.log("prop "+prefix+" found")
        elBlock.classList.remove(block_hide)
        elLabel.innerText = cfgCmd[prefix].info
        elFile.accept =(cfgCmd[prefix].hasOwnProperty('extensions'))?cfgCmd[prefix].extensions:""
        const allowMultiple = cfgCmd[prefix].hasOwnProperty('multiple') && cfgCmd[prefix].multiple
        if (allowMultiple) {
            elFile.setAttribute("multiple","")
        } else {
            elFile.removeAttribute("multiple")
        }
    } else {
        console.log("prop "+prefix+" miss")
        elBlock.classList.add(block_hide)
    }

}

function isNullOrUndefined(value) {
  return value === undefined || value === null;
}