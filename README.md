# Задание на стажировку "JBR Genome Browser"

Проект выполнен на языке Kotlin, в проект для примера включен файл human2.bed. Использован 14й JDK. 
Для проекта настроено несколько конфигураций запуска:
1. *MainKt* - идексирует файл human2.bed и предлагает пользователю вручную ввести данные для поиска. Можно ввести имя хромосомы, начало и конец диапазона поиска через пробел. Будут выведены все найденные результаты
2. *IndexTest* - запуск unit-тестов (используется Kotlin.test)

## Ключевые особенности
1. Проект реализует все интерфейсы данные в задании
2. Индекс представляет собой словарь, где **ключом является названия хромосомы**, а **значением - координаты начала и конеца участка** (значения сортируются по координате начала, это будет использовано при выполнении поиска). Из-за использования HashMap названия имплементаций - `HashIndex` и `HashIndexBedReader`
3. При поиске по индексу алгоритм определяет выделяет с какого элемента нужно начинать поиск по координате начала, а затем проверяет все оставшиеся по координате конца. 
4. Результатом поиска по индексу является список номеров строк с искомыми данными, которые затем читаются из .bed файла.
5. Алгоритм может работать с неотсортированным файлом (так и не понял, может ли такое быть вообще). Результаты поиска сортируются по координате начала. 
6. Сохранение и загрузка индекса реализованы через стандартную сериализацию объектов в Java. Это не самый быстрый способ, сделать все через бинарный файл было бы быстрее. Но так как, вы сказали, что особых требований к производительности не выдвигается - оставил так
7. В проекте в нескольких местах использован Experimental Kotlin Path API, надеюсь, что это не страшно
8. Написаны комментарии и Unit-тесты. Для классов `HashIndexBedReader` и `HashIndex` обеспечено 100% покрытие тестами. 

## Примечания
1. В HashIndex пришлось добавить свой метод `findInIndex`, производящий поиск по индексу и возвращающий номера искомых строк в .bed файле. Я думаю, что его стоит добавить в интерфейс `BedIndex`, но делать я этого не стал, так как этого не сказано в задании. Признаю, что в данной рализации возможна ситуация, когда в мой `HashIndexBedReader` может быть передан любой другой объект, реализующий `BedIndex` (хотя в нём нечего реализовывать), и будет выброшен `ClassCastException` из-за попытки привести его к `HashIndex`. 