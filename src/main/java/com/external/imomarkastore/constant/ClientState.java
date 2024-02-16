package com.external.imomarkastore.constant;

import lombok.Getter;

public enum ClientState {
    INITIAL_START("Первое включение"),
    REPEATED_START("Повторное включение"),
    MAIN_MENU("Главное меню"),
    INITIAL_SET_NAME("Ввод имени"),
    INITIAL_SET_PHONE_NUMBER("Ввод номера телефона"),
    CREATE_APPLICATION("Создание заявки"),
    INSERT_CAR_DETAILS("Ввод данных автомобиля"),
    CHOOSE_CAR_FOR_APPLICATION("Выбор автомобиля для заявки"),
    INSERT_VIN_NUMBER("Ввод VIN-номера"),
    INSERT_MAIN_PURPOSE("Ввод поломки или описание проблемы"),
    INSERT_COMMENT("Ввод комментария"),
    EDIT_NAME("Изменение имени"),
    EDIT_PHONE_NUMBER("Изменение номера телефона"),
    SAVE_NAME("Ввод и сохранение имени"),
    SAVE_PHONE_NUMBER("Ввод и сохранение номера телефона"),
    GET_CARS("Вывод автомобилей"),
    DELETE_CAR("Удаление автомобиля"),
    BACK_TO_MAIN_MENU("Возврат к главному меню");

    @Getter
    private final String clientStateText;

    ClientState(String clientStateText) {
        this.clientStateText = clientStateText;
    }
}
