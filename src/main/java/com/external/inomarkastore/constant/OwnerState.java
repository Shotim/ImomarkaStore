package com.external.inomarkastore.constant;

import lombok.Getter;

public enum OwnerState {

    START("Старт"),
    MAIN_MENU("Главное меню"),
    GET_APPLICATIONS("Получение активных заявок"),
    EXPORT_APPLICATIONS("Экспорт заявок"),
    MOVE_APPLICATION_TO_ARCHIVE("Перенос заявки в архив"),
    GET_ARCHIVED_APPLICATIONS("Получение архивных заявок"),
    DELETE_APPLICATION("Удаление заявки"),
    RESTORE_APPLICATION("Активация заявки"),
    BACK_TO_MAIN_MENU("Возвращение в главное меню"),
    GET_CLIENTS("Получение списка клиентов"),
    MOVE_CLIENT_TO_BLACK_LIST("Перенос клиента в черный список"),
    GET_BLACK_LIST("Получение черного списка"),
    BACK_FROM_BLACK_LIST("Перенос клиента из черного списка"),
    GET_CONTACTS("Получение контактов"),
    EDIT_NAME("Изменение имени"),
    EDIT_PHONE_NUMBER("Изменение номера телефона"),
    EDIT_ADDRESS("Изменение адреса"),
    EDIT_INN("Изменение ИНН"),
    EDIT_EMAIL("Изменение email-а"),
    EDIT_WORKING_HOURS("Изменение времени работы"),
    SAVE_NAME("Сохранение имени"),
    SAVE_PHONE_NUMBER("Сохранение номера телефона"),
    SAVE_ADDRESS("Сохранение адреса"),
    SAVE_INN("Сохранение ИНН"),
    SAVE_EMAIL("Сохранение email-а"),
    SAVE_WORKING_HOURS("Сохранение времени работы"),
    PREPARE_PAYMENT("Подготовка к отправке оплаты"),
    SEND_PAYMENT("Отправить оплату"),
    PREPARE_GET_PHOTO("Поиск фото по идентификатору"),
    GET_PHOTO("Получение фото по идентификатору"),
    SET_PAYMENT("Пометка заявки как оплаченной"),
    CONFIRM_PAYMENT("Подтверждение оплаты заявки"),
    PREPARE_DISTRIBUTION("Подготовка к отправке рассылки"),
    SEND_DISTRIBUTION("Отправка рассылки");

    @Getter
    private final String ownerStateText;

    OwnerState(String ownerStateText) {
        this.ownerStateText = ownerStateText;
    }
}