/**
 * test
 * test
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

export interface Dog { 
    bark?: boolean;
    breed?: Dog.BreedEnum;
}
export namespace Dog {
    export type BreedEnum = 'Dingo' | 'Husky' | 'Retriever' | 'Shepherd';
    export const BreedEnum = {
        Dingo: 'Dingo' as BreedEnum,
        Husky: 'Husky' as BreedEnum,
        Retriever: 'Retriever' as BreedEnum,
        Shepherd: 'Shepherd' as BreedEnum
    };
}