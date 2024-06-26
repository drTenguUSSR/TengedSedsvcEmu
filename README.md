# TengedSedsvcEmu описание

## текущая реализация

[пример front-end реализации](https://drtenguussr.github.io/TengedSedsvcEmu/staticA/index.html)

[код эмулятора backend. serverA](https://github.com/drTenguUSSR/TengedSedsvcEmu/tree/main/serverA)

## назначение проекта

TengedSedsvcEmu пет-проект реализующий интерфейс СЭДсервиса

[СЭД-сервис в рамках CMJ](https://sup.inttrust.ru:8446/prjdocs/master/specs/sedsvc/index.html)

[СЭДсервис как отдельный компонент](https://sup.inttrust.ru:8446/prjdocs/sedsvc/master/specs/sedsvc/index.html)

## глобальные версии и основные особенности

- 1.x - реализация front-end на базе HTML5+CSS3+JS
    позволяющая максимально имитировать отправку образцов запросов.
    кнопки отправки запросов генерируются исходя из YAML

    планируемый-недостаток-1: невозможно поставить закладку на конкретную функцию
    в СЭДсервисе т.к. приложение SPA (планируется) и весь интерфейс будет динамическим.
    *прим*: возможно, этот недостаток будет устранить в 2.x+

- 2.x - создание имитатора back-end СЭДсервиса. принимающего
    и минимально валидирующего запросы и возвращающего фиксированные
    файлы в виде ответа. технология: SpringBoot актуальной версии +
    язык Kotlin + сборка Maven + JDK 17.
    прим: JDK 17 - согласно [https://start.spring.io/](https://start.spring.io/)
    это минимальная версия для SpringBoot 3.2.3

## план по версиям до 1.x

### 0.0.2 обновление

работает:

    - загрузка-парсинг yaml конфига api/data-main.yaml

    - настройка entrypoint + загрузка/парсинг JSON по указанному адресу

    - настройка content-type

    - настройка form-action

    - автоматическая загрузка первого сервиса при открытии страницы

### 0.0.1 начальный заливка и POC код

- проработан макета HTML главной страницы (HTML5,CSS3,БЭМ), верстка на flex для гибкости
- определение технологии для редактирования JSON - [jsoneditor 10.0.1](https://www.npmjs.com/package/jsoneditor)
- определение технологии для парсинга YML конфигурации - [js-yaml@4.1.0](https://www.npmjs.com/package/js-yaml)
- index.html: оптимизация крутящегося кубика - убрано в фон
- t1-button-decorator.html: ссылки, выглядевшие как кнопки (CSS)
- t2-static-2elem.html: верстка горизонтального и вертикального меню
    на базе ссылок-кнопок
- t3-json-edit.html: проверка работоспособности jsoneditor при подключении к html
- t4-button-link.html:
    - подключение js-yaml для парсинга фиксированного yaml-файла (async, await)
    - подгонка отправляемого контента с файлом к формату СЭДсервиса
    - реализация возможности отображения и редактирования ссылки, по которой
    будет отправлен POST запрос
    - отправка производится через типовую кнопку button типа submit
    - кнопки выбора файлов захадкожены 3 штуки - через javascript будет управление
        видимостью лишних кнопок
    - сделано отображение информации о выбранных файлов (выводится имя и
        длина в байтах для каждого выбранного файла)
- проверка возможности публикации такого front-end куска с корректными
    ссылками на CSS/JS
- документация в формате md проходит проверку markdownlint

## недостатки текущей реализации и направление их устранения (неупорядоченный)

<table>
  <caption>
    TengedSedsvcEmu
  </caption>
  <thead class="header">
    <tr>
      <th width="10%">№</th>
      <th width="45%">оригинальная реализация и проблемы</th>
      <th width="45%">TengedSedsvcEmu</th>
    </tr>
  </thead>
<tbody>
<tr><td align="right">1</td><td valign="top">
    Для отображения пользовательского интерфейса
    используется HTML 3.2/4.0 внутри JSP страницы
</td><td valign="top">
    Для отображения пользовательского интерфейса
    используется HTML5 + CSS3. применяются семантические теги и БЭМ
</td></tr>

<tr><td align="right">2</td><td valign="top">
    для определения url точки доступа применяется серверный компонент,
    написанный на java
    <ul>
    <li>пояснение-1: если URL точки входа определяется сервером, то
        корректный проброс порта через промежуточный сервер (proxy)
        становится затруднительным</li>
    <li>
    пояснение-2: если адрес определен не верно, то нет возможности его исправить
    </li>
</td><td valign="top">
    точка входа сервера определяется клиентом, исходя из текущего адреса
    страницы и имеется возможность ее редактирования
</td></tr>

<tr><td align="right">3</td><td valign="top">
    серверная сторона написана на Java 8 + Spring 4.1
    </td><td valign="top">
    перевод на Java JVM актуальной версии LTS (21?)
    + SpringBoot + Kotlin
    <br>(план 2.x)
</td></tr>

<tr><td align="right">4</td><td valign="top">
    в качестве запроса можно указать произвольный текст.
    валидации не производится
    </td><td valign="top">
    принудительно проверять как минимум валидность JSON (реализация в 1.x)
    <br>
    TODO: использовать jsoneditor для проверки по схеме
</td></tr>

<tr><td align="right">5</td><td valign="top">
    при добавлении новых сервисов для СЭДсервиса производится:
    <ul>
        <li>копи-паста jsp страницы</li>
        <li>правка в странице адреса отправки</li>
        <li>правка в странице примера запроса</li>
        <li>добавление новой строки в index, где перечислены все
        образцы запросов</li>
    </ul>
    </td><td valign="top">
        перечень запросов и их содержание создается на основе данных YAML
        файла. для добавления нового сервиса необходимо и достаточно
        добавить его описание в YAML файл
        <br>
        TODO: возможность выбирать нужный YAML-файл, в зависимости
        от обнаруженной версии СЭДсервиса
</td></tr>

<tr><td align="right">6</td><td valign="top">
    проблема обработки multipart/response в ответе СЭДсервиса - актуальные
    браузеры не поддерживают парсинг такого ответа и сохраняют весь ответ
    в виде одного файла со всеми служебными заголовками.
    <ul>
        <li>решение 1: использование старого браузера FireFox версии 70.0.1
        и не обновлять его</li>
        <li>решение 2: добавлять в запрос недокументированный параметр
            makeZip = true (название может быть неточным), чтобы СЭДсервис
            возвращал не multipart/response, а один zip файл, НО
            это будет нарушением спецификации т.к. такой вариант не предусмотрен
    </td><td valign="top">
    TODO: реализация через актуальный JavaScript получение потока байт
    и выдача диалога сохранения для каждой части ответа (теоретически)
</td></tr>

<tr><td align="right">7</td><td valign="top">
    front-end вмурован в СЭДсервис и обновляется только вместе с ним.
    т.к. спецификация выложена в открытый доступ, логично разделять
    front-end и back-end
    </td><td valign="top">
    (для 1.x) front-end код полностью автономен и может работать с
    отдельно стоящим произвольным экземпляром СЭДсервиса
</td></tr>

<tr><td align="right">8</td><td valign="top">
    (ограничение спроектированного протокола) для передачи файлов
    и получения ответов от СЭДсервиса всегда используется HTTP.
    это несет дополнительные накладные расходы времени вызова
    и лишних операций чтения-записи если клиент СЭДсервиса
    и сам СЭДсерви находятся на одном хосте
    </td><td valign="top">
    TODO: для поддержки на стороне СЭДсервиса такой возможности,
    нужно расширять протокол и, соотвественно, менять код клиентов
    TODO: для повышения скорости, можно рассмотреть поддержку
    WebSockets, но проблемы будут такими же
</td></tr>

</tbody>
</table>

## полезная информация

[полезная информация](https://drtenguussr.github.io/#useful-info-links)
